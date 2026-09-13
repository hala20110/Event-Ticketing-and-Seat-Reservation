package com.netpoint.ticketing.Controller;

import com.netpoint.ticketing.DTO.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {} path:{}", msg, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(msg, 400, req.getRequestURI()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> badCreds(BadCredentialsException ex, HttpServletRequest req) {
        log.warn("Bad credentials path:{}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid email or password", 401, req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> denied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Access denied path:{}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("You don't have permission to do this", 403, req.getRequestURI()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> generic(RuntimeException ex, HttpServletRequest req) {
        log.error("Unhandled exception path:{}", req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ex.getMessage(), 500, req.getRequestURI()));
    }

    @ExceptionHandler(com.netpoint.ticketing.Exceptions.ReservationException.class)
    public ResponseEntity<ErrorResponse> reservation(com.netpoint.ticketing.Exceptions.ReservationException ex,
                                                     HttpServletRequest req) {
        log.warn("Reservation error: {} path:{}", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), 409, req.getRequestURI()));
    }
    @ExceptionHandler(com.netpoint.ticketing.Exceptions.PaymentException.class)
    public ResponseEntity<ErrorResponse> payment(com.netpoint.ticketing.Exceptions.PaymentException ex,
                                                 jakarta.servlet.http.HttpServletRequest req) {
        log.warn("Payment error: {} path:{}", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), 400, req.getRequestURI()));
    }
}