package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.dto.UserDto;
import com.example.supplychainmanagement.entity.Product;
import com.example.supplychainmanagement.service.api.UserService;
import com.example.supplychainmanagement.service.business.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
@Component
public class PublicController {
    private final MeterRegistry meterRegistry;

    private final UserService userService;
    private final ProductService productService;
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public PublicController(MeterRegistry meterRegistry, UserService userService, ProductService productService) {
        this.meterRegistry = meterRegistry;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUsers() {
        try {
            double uptime = meterRegistry.get("process.uptime").timeGauge().value(TimeUnit.MINUTES);

            List<UserDto> users = userService.findAllUsers();
            long usersAmount = users.size();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.createObjectNode();
            jsonObject.put("Registered users", usersAmount);
            jsonObject.put("Status", HttpStatus.OK.value());
            jsonObject.put("Server Name", System.getProperty("os.name"));
            jsonObject.put("Server Version", System.getProperty("os.version"));
            jsonObject.put("Server Architecture", System.getProperty("os.arch"));
            jsonObject.put("Logical cores", (int) meterRegistry.get("system.cpu.count").gauge().value());
            jsonObject.put("Server Uptime in min.", decimalFormat.format(uptime));
            jsonObject.put("App started at", meterRegistry.get("application.started.time").timeGauge().value());
            return new ResponseEntity<>(jsonObject, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> productList = productService.getProducts();
            return new ResponseEntity<>(productList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/ping")
    public String pingSimple() {
        return "pong";
    }

    @GetMapping(value = "/ping.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> pingJson() {
        Map<String, String> body = new HashMap<>();
        body.put("message", "pong");
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
