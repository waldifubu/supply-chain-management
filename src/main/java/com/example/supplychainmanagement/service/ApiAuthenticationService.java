package com.example.supplychainmanagement.service;

import com.example.supplychainmanagement.auth.AuthenticationRequest;
import com.example.supplychainmanagement.auth.AuthenticationResponse;
import com.example.supplychainmanagement.auth.RegisterRequest;
import com.example.supplychainmanagement.dto.UserDto;
import com.example.supplychainmanagement.model.enums.UserRole;
import com.example.supplychainmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiAuthenticationService {
    //@TODO: Remove repo, use userService instead
    private final UserRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthenticationResponse register(RegisterRequest request) {
        UserDto userDto = new UserDto();
        userDto.setFirstName(request.getFirstname());
        userDto.setLastName(request.getLastname());
        userDto.setEmail(request.getEmail());
        userDto.setPassword(request.getPassword());

        if (!UserRole.contains(request.getRole())) {
            throw new EnumConstantNotPresentException(UserRole.class, request.getRole());
        }

        userDto.setRole(UserRole.valueOf(request.getRole()));

        userService.saveUser(userDto);
        var jwtToken = jwtService.generateToken(userDto);
        var time = jwtService.expirationTimeAsString(jwtToken);
        return AuthenticationResponse.builder().token(jwtToken).expirationTime(time).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = repository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(userService.convertEntityToDto(user));
        var time = jwtService.expirationTimeAsString(jwtToken);
        return AuthenticationResponse.builder().token(jwtToken).expirationTime(time).build();
    }
}