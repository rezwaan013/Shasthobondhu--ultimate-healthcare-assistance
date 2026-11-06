/**
 * ShasthoBondhu – password-reset backend.
 *
 * Two HTTPS Cloud Functions back the "forgot password" screen:
 *
 *   POST /sendCode        { "email": "user@gmail.com" }
 *   POST /resetPassword   { "email": "...", "code": "123456", "newPassword": "..." }
 *
 * The Gmail credentials never leave the server. Configure them once:
 *
 *   firebase functions:config:set gmail.user="youraccount@gmail.com" \
 *                              gmail.app_password="16-char-app-password"
 *
 * (Create the app password at https://myaccount.google.com/apppasswords – a
 * normal Gmail password will not work while 2-Step Verification is on.)
 *
 * Deploy:
 *   firebase deploy --only functions
 *
 * The deployed URLs look like:
 *   https://asia-south1-<project-id>.cloudfunctions.net/
 * which is exactly the value to pass as SHASSTHO_RESET_URL.
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");
const crypto = require("crypto");

admin.initializeApp();
const db = admin.firestore();

const CODE_TTL_MS = 10 * 60 * 1000;
const MAX_ATTEMPTS = 5;
const REGION = "asia-south1";

const mailer = nodemailer.createTransport({
    service: "gmail",
    auth: {
        user: functions.config().gmail.user,
        pass: functions.config().gmail.app_password,
    },
});

const sha256 = (value) => crypto.createHash("sha256").update(value).digest("hex");
const normalize = (email) => String(email || "").trim().toLowerCase();
const docIdFor = (email) => sha256("reset:" + email);
const generateCode = () => String(crypto.randomInt(100000, 1000000));

const json = (res, status, payload) => res.status(status).json(payload);

exports.sendCode = functions
    .region(REGION)
    .https.onRequest(async (req, res) => {
        if (req.method !== "POST") {
            return json(res, 405, {success: false, message: "Method not allowed"});
        }

        const email = normalize(req.body && req.body.email);
        if (!email || !email.includes("@")) {
            return json(res, 400, {success: false, message: "Enter a valid email address"});
        }

        // Always answer the same way so the endpoint cannot be used to discover accounts.
        try {
            await admin.auth().getUserByEmail(email);
        } catch (error) {
            return json(res, 200, {success: true, message: "Code sent"});
        }

        const code = generateCode();
        await db.collection("password_resets").doc(docIdFor(email)).set({
            email,
            codeHash: sha256(email + ":" + code),
            expiresAt: Date.now() + CODE_TTL_MS,
            attempts: 0,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        try {
            await mailer.sendMail({
                from: `"ShasthoBondhu" <${functions.config().gmail.user}>`,
                to: email,
                subject: "Your ShasthoBondhu verification code",
                text: `Your password reset code is ${code}. It expires in 10 minutes.`,
                html: `
                    <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto">
                      <h2 style="color:#D32F2F;margin-bottom:8px">ShasthoBondhu</h2>
                      <p>Use the code below to reset your password.</p>
                      <p style="font-size:32px;letter-spacing:8px;font-weight:bold;
                                background:#F5F5F5;padding:16px;text-align:center;
                                border-radius:12px">${code}</p>
                      <p style="color:#757575">This code expires in 10 minutes.
                         If you did not request it, you can safely ignore this email.</p>
                    </div>`,
            });
        } catch (error) {
            console.error("sendCode mail failure", error);
            return json(res, 500, {success: false, message: "Could not send the email"});
        }

        return json(res, 200, {success: true, message: "Code sent"});
    });

exports.resetPassword = functions
    .region(REGION)
    .https.onRequest(async (req, res) => {
        if (req.method !== "POST") {
            return json(res, 405, {success: false, message: "Method not allowed"});
        }

        const body = req.body || {};
        const email = normalize(body.email);
        const code = String(body.code || "").trim();
        const newPassword = String(body.newPassword || "");

        if (!email || !/^\d{6}$/.test(code)) {
            return json(res, 400, {success: false, message: "Incorrect or expired code"});
        }
        if (newPassword.length < 8 || !/[A-Za-z]/.test(newPassword) || !/\d/.test(newPassword)) {
            return json(res, 400, {
                success: false,
                message: "Password must be at least 8 characters and include a letter and a number",
            });
        }

        const ref = db.collection("password_resets").doc(docIdFor(email));
        const snapshot = await ref.get();
        if (!snapshot.exists) {
            return json(res, 400, {success: false, message: "Incorrect or expired code"});
        }

        const data = snapshot.data();
        if (Date.now() > data.expiresAt) {
            await ref.delete();
            return json(res, 400, {success: false, message: "Incorrect or expired code"});
        }
        if ((data.attempts || 0) >= MAX_ATTEMPTS) {
            await ref.delete();
            return json(res, 429, {success: false, message: "Too many attempts. Request a new code."});
        }

        const matches = sha256(email + ":" + code) === data.codeHash;
        if (!matches) {
            await ref.update({attempts: admin.firestore.FieldValue.increment(1)});
            return json(res, 400, {success: false, message: "Incorrect or expired code"});
        }

        try {
            const user = await admin.auth().getUserByEmail(email);
            await admin.auth().updateUser(user.uid, {password: newPassword});
            await ref.delete();
            return json(res, 200, {success: true, message: "Password updated"});
        } catch (error) {
            console.error("resetPassword failure", error);
            return json(res, 500, {success: false, message: "Could not update the password"});
        }
    });
