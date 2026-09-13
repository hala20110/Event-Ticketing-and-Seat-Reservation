package com.netpoint.ticketing.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private String message;
    private int statusCode;
    private String requestURI;
    private LocalDateTime timestamp;

    public ErrorResponse(String message, int statusCode, String requestURI) {
        this.message = message;
        this.statusCode = statusCode;
        this.requestURI = requestURI;
        this.timestamp = LocalDateTime.now();
    }
}