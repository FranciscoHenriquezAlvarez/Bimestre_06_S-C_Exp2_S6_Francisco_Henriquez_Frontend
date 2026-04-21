package com.duoc.frontend.service;

import org.springframework.http.HttpStatusCode;

public class BackendServiceException extends RuntimeException {

    private final HttpStatusCode statusCode;

    public BackendServiceException(HttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public boolean isUnauthorized() {
        return statusCode.value() == 401;
    }
}
