package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.auth.AuthenticationRequest;
import com.example.supplychainmanagement.auth.AuthenticationResponse;
import com.example.supplychainmanagement.auth.RegisterRequest;
import com.example.supplychainmanagement.event.UserLoginEvent;
import com.example.supplychainmanagement.service.api.ApiAuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final ApiAuthenticationService apiAuthenticationService;

    private final ApplicationEventPublisher eventPublisher;

    public AuthApiController(ApiAuthenticationService apiAuthenticationService, ApplicationEventPublisher eventPublisher) {
        this.apiAuthenticationService = apiAuthenticationService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(apiAuthenticationService.register(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error while registration: " + e.getMessage());
        }
    }

    @PostMapping(value = {"/authenticate", "/login"})
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = apiAuthenticationService.authenticate(request);
            eventPublisher.publishEvent(new UserLoginEvent(response.getUsername()));
            return ResponseEntity.ok(response);
        } catch (ExpiredJwtException bce) {
            AuthenticationResponse exp = AuthenticationResponse.builder().token("Token expired").build();
            return ResponseEntity.ok(exp);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Login failed: " + ex.getMessage());
        }
    }
}
