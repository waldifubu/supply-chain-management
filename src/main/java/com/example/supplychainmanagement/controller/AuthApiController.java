package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.auth.AuthenticationRequest;
import com.example.supplychainmanagement.auth.AuthenticationResponse;
import com.example.supplychainmanagement.auth.RegisterRequest;
import com.example.supplychainmanagement.service.api.ApiAuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final ApiAuthenticationService apiAuthenticationService;

    public AuthApiController(ApiAuthenticationService apiAuthenticationService) {
        this.apiAuthenticationService = apiAuthenticationService;
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
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = apiAuthenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (ExpiredJwtException bce) {
            //throw new Exception("Incorrect username or password", e);
            AuthenticationResponse exp = AuthenticationResponse.builder().token("Token expired").build();
            return ResponseEntity.ok(exp);
        }
    }
}
