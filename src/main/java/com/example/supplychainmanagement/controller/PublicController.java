package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.dto.UserDto;
import com.example.supplychainmanagement.service.api.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicController {

    private final UserService userService;

    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUsers() {
        try {
            List<UserDto> users = userService.findAllUsers();
            long usersAmount = users.size();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.createObjectNode();
            jsonObject.put("registeres users", usersAmount);
            jsonObject.put("status", HttpStatus.OK.value());

            return new ResponseEntity<>(jsonObject, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
