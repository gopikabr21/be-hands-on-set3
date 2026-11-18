package com.roombooking.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, String> messages;

    public ApiException(HttpStatus status, Map<String, String> messages) {
        super(messages.values().stream().findFirst().orElse(status.getReasonPhrase()));
        this.status = status;
        this.messages = Map.copyOf(messages);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getMessages() {
        return messages;
    }
}
