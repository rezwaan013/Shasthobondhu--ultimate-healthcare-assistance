package com.shasthobondhu.app.data.remote.model;

/** Generic response returned by the password-reset backend. */
public class SimpleResponse {
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
