package com.shasthobondhu.app.data.remote;

import com.shasthobondhu.app.data.remote.model.ResetPasswordRequest;
import com.shasthobondhu.app.data.remote.model.SendCodeRequest;
import com.shasthobondhu.app.data.remote.model.SimpleResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Backend contract for the email-based password reset flow.
 *
 * <p>The client never holds mail credentials. The server:
 * <ul>
 *     <li>{@code POST /sendCode} – generates a 6-digit code, stores its hash with a
 *     10 minute expiry, and emails it through Gmail SMTP / SendGrid / any provider.</li>
 *     <li>{@code POST /resetPassword} – verifies the code and updates the user's
 *     Firebase Auth password with the Admin SDK.</li>
 * </ul>
 *
 * <p>A reference implementation is provided in {@code functions/index.js}.</p>
 */
public interface PasswordResetApi {

    @POST("sendCode")
    Call<SimpleResponse> sendCode(@Body SendCodeRequest request);

    @POST("resetPassword")
    Call<SimpleResponse> resetPassword(@Body ResetPasswordRequest request);
}
