package com.shasthobondhu.app.data.model;

/** Immutable loading/success/empty/error state for repository-backed UI. */
public final class Resource<T> {

    public enum Status { LOADING, SUCCESS, EMPTY, ERROR }
    public enum ErrorType { NETWORK, SERVER, MALFORMED }

    public final Status status;
    public final T data;
    public final ErrorType errorType;

    private Resource(Status status, T data, ErrorType errorType) {
        this.status = status;
        this.data = data;
        this.errorType = errorType;
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> empty() {
        return new Resource<>(Status.EMPTY, null, null);
    }

    public static <T> Resource<T> error(ErrorType type) {
        return new Resource<>(Status.ERROR, null, type);
    }
}
