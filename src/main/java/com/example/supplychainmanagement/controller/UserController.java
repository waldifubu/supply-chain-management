package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.dto.UserDto;
import com.example.supplychainmanagement.service.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
//    @Secured({"ROLE_CUSTOMER", "ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<List<UserDto>> getUsers() {
        try {
            List<UserDto> users = userService.findAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
