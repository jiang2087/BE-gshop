package com.example.demo.rag;

import lombok.Getter;

@Getter
public class ChatClientException extends RuntimeException {
    private final Integer statusCode;

    public ChatClientException(String message) {
        super(message);
        this.statusCode = null;
    }

    public ChatClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public ChatClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ChatClientException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public boolean hasStatusCode() {
        return statusCode != null;
    }

    public boolean isClientError() {
        return statusCode != null && statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode != null && statusCode >= 500;
    }

    public boolean isRateLimitError() {
        return statusCode != null && statusCode == 429;
    }
}
