package com.shasthobondhu.app.data.remote.model;

/** Payload for verifying the emailed code and setting a new password. */
public class ResetPasswordRequest {
    private final String email;
    private final String code;
    private final String newPassword;

    public ResetPasswordRequest(String email, String code, String newPassword) {
        this.email = email;
        this.code = code;
        this.newPassword = newPassword;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
