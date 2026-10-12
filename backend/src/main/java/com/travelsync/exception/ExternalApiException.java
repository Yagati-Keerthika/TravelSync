package com.travelsync.exception;

public class ExternalApiException extends ApiException {
    private final String apiName;
    private final int statusCode;

    public ExternalApiException(String apiName, String message) {
        super(String.format("External API [%s] error: %s", apiName, message));
        this.apiName = apiName;
        this.statusCode = 502;
    }

    public ExternalApiException(String apiName, String message, int statusCode) {
        super(String.format("External API [%s] (status %d): %s", apiName, statusCode, message));
        this.apiName = apiName;
        this.statusCode = statusCode;
    }

    public ExternalApiException(String apiName, String message, Throwable cause) {
        super(String.format("External API [%s] error: %s", apiName, message), cause);
        this.apiName = apiName;
        this.statusCode = 502;
    }

    public String getApiName() {
        return apiName;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
