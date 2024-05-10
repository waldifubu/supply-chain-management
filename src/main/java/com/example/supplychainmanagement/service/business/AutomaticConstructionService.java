package com.example.supplychainmanagement.service.business;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class AutomaticConstructionService {

    @Scheduled(initialDelay = 10, fixedDelay  = 150, timeUnit = TimeUnit.SECONDS)
    public void construction() {
        System.out.println("Tried assembling "+ LocalDateTime.now());
    }
}
