package com.shasthobondhu.app.data.remote.model;

/** Payload for requesting a 6-digit email verification code. */
public class SendCodeRequest {
    private final String email;

    public SendCodeRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
