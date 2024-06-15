package com.example.supplychainmanagement.service.listener;

import com.example.supplychainmanagement.event.UserLoginEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class UserLoginListener {

    protected final Log logger = LogFactory.getLog(this.getClass());
    private final Environment env;

    public UserLoginListener(Environment env) {
        this.env = env;
    }

    @EventListener(UserLoginEvent.class)
    @Async
    public void handleUserLoginEvent(UserLoginEvent event) {
        logger.info("User logged in: " + event.getUsername());
        if (Boolean.parseBoolean(env.getProperty("app.debug"))) {
            System.out.println("User logged in: " + event.getUsername());
        }
    }
}