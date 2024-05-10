package com.example.supplychainmanagement.service.listener;

import com.example.supplychainmanagement.event.UserLoginEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class UserRegistrationListener {

    protected final Log logger = LogFactory.getLog(this.getClass());

    @EventListener(UserLoginEvent.class)
    @Async
    public void handleUserLoginEvent(UserLoginEvent event) {
        logger.info("User logged in: " + event.getUsername());
    }
}