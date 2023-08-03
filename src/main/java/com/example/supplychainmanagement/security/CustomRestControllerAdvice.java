package com.example.supplychainmanagement.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;

@RestControllerAdvice
public class CustomRestControllerAdvice {

    //    @ExceptionHandler(BadCredentialsException.class) // exception handled
    public ResponseEntity<ErrorResponse> handleNullPointerExceptions(
            Exception e
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage()
                ),
                status
        );
    }

    @ExceptionHandler(ExpiredJwtException.class) // exception handled
    public ResponseEntity<ErrorResponse> handleALlExceptions(
            ExpiredJwtException e
    ) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String message = "Token expired. Please login before.";

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        message,
                        this.getClass().getSimpleName()
                ),
                status
        );
    }


    // fallback method
    @ExceptionHandler(WeakKeyException.class) // exception handled
    public ResponseEntity<ErrorResponse> handleExceptions(Exception e) {
        HttpStatus status = HttpStatus.FORBIDDEN; // 500

        // converting the stack trace to String
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = "Key zu schwach"; //stringWriter.toString();

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        stackTrace // specifying the stack trace in case of 500s
                ),
                status
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> notFound(Exception e) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        // converting the stack trace to String
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = "Route not found " + e.getClass().getSimpleName(); //stringWriter.toString();

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        stackTrace // specifying the stack trace in case of 500s
                ),
                status
        );
    }


    @ExceptionHandler({DataIntegrityViolationException.class, SQLIntegrityConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> integration(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // converting the stack trace to String
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = "SQL Problem :" + e.getClass().getSimpleName(); //stringWriter.toString();

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        stackTrace // specifying the stack trace in case of 500s
                ),
                status
        );
    }


    @ExceptionHandler(EnumConstantNotPresentException.class)
    public ResponseEntity<ErrorResponse> enums(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        String stackTrace = "Unknown value. Given value has no matching mapping: " + e.getClass().getSimpleName(); //stringWriter.toString();
        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        stackTrace // specifying the stack trace in case of 500s
                ),
                status
        );
    }


    // Data are missing
    @ExceptionHandler(NullPointerException.class) // exception handled
    public ResponseEntity<ErrorResponse> NullPointerExceptions(
            Exception e
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String simpleName = this.getClass().getSimpleName();

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        simpleName,
                        Arrays.stream(e.getStackTrace()).toList()
                ),
                status
        );
    }


    @ExceptionHandler(HttpClientErrorException.BadRequest.class) // exception handled
    public ResponseEntity<ErrorResponse> BadRequestException(
            Exception e
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String stackTrace = this.getClass().getSimpleName();

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        stackTrace
                ),
                status
        );
    }

    @ExceptionHandler(HttpClientErrorException.MethodNotAllowed.class) // exception handled
    public ResponseEntity<ErrorResponse> MethodNotAllowed(
            Exception e
    ) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        String stackTrace = this.getClass().getSimpleName();

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        stackTrace
                ),
                status
        );
    }
}
