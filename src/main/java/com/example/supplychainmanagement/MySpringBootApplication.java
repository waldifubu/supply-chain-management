package com.example.supplychainmanagement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MySpringBootApplication {

    private static final Logger logger = LogManager.getLogger(MySpringBootApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApplication.class, args);

        if (logger.isDebugEnabled()) {
            logger.debug("Hello from Log4j - num : {}", "123");
        }
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            /*
            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
            */
        };
    }
}
