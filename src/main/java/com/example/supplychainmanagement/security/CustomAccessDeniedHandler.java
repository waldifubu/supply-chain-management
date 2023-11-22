package com.example.supplychainmanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    // Jackson JSON serializer instance
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @ResponseBody
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN; // 403

        Map<String, Object> data = new HashMap<>();
        data.put(
                "timestamp",
                new Date()
        );
        data.put(
                "code",
                httpStatus.value()
        );
        data.put(
                "status",
                httpStatus.name()
        );
        data.put(
                "message",
                exception.getMessage()
        );

        // setting the response HTTP status code
        response.setStatus(httpStatus.value());
        response.setHeader("Content-Type", "application/json");

        // serializing the response body in JSON


        response
                .getOutputStream()
                .print(
                        objectMapper.writeValueAsString(data)
                );


//        response.getOutputStream().println("ERROR");
    }
}
