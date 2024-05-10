package com.example.supplychainmanagement.security;

import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class CustomControllerAdvice {


    @ExceptionHandler(AccessDeniedException.class) // exception handled
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public String handleNotAllowed(AccessDeniedException ex) {
        return "login";
    }



    // fallback method
//    @ExceptionHandler(Exception.class) // exception handled
    public ResponseEntity<ErrorResponse> handleExceptions(Exception e) {
        // ... potential custom logic

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500

        // converting the stack trace to String
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = "Alle"; //stringWriter.toString();


        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        this.getClass().getSimpleName() // specifying the stack trace in case of 500s
                ),
                status
        );
    }

    //    @ExceptionHandler(BadCredentialsException.class) // exception handled
    public ResponseEntity<ErrorResponse> handleExceptions2(Exception e) {
        // ... potential custom logic

        HttpStatus status = HttpStatus.NO_CONTENT;

        // converting the stack trace to String
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = this.getClass().getSimpleName(); // "Alle"; //stringWriter.toString();


        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        stackTrace // specifying the stack trace in case of 500s
                ),
                status
        );
    }


    @ExceptionHandler(SignatureException.class) // exception handled
    public ResponseEntity<ErrorResponse> signatureFailed(Exception e) {
        // ... potential custom logic

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        // converting the stack trace to String
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = "Alle"; //stringWriter.toString();


        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        "Token is invalid. Try login again."
                ),
                status
        );
    }



/*
    @ExceptionHandler(ExpiredJwtException.class) // exception handled
    public ResponseEntity<ErrorResponse> handleALlExceptions(
            ExpiredJwtException e
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        return new ResponseEntity<>(
                new ErrorResponse(
                        status,
                        e.getMessage(),
                        this.getClass().getSimpleName()
                ),
                status
        );
    }
 */
}
