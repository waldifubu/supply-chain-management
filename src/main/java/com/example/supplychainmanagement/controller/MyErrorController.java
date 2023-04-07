package com.example.supplychainmanagement.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
// org.springframework.security.authentication.BadCredentialsException: Bad credentials
@Controller
public class MyErrorController implements ErrorController, AuthenticationEntryPoint {
    // @TODO: Not working
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        HttpStatus status2 = HttpStatus.INTERNAL_SERVER_ERROR; // 500

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error-404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error-403";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error-404";
            }
        }
        return "error";
    }

    // Access denied!
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof InsufficientAuthenticationException) {
            response.sendRedirect("/login");
            return;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
    }
}
