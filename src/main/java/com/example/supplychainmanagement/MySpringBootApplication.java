package com.example.supplychainmanagement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@SpringBootApplication
public class MySpringBootApplication implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(MySpringBootApplication.class);

    private final Environment env;

    public MySpringBootApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (logger.isDebugEnabled()) {
            logger.debug("{}", env.getProperty("app.name"));
        }

        logger.info("{} is running...", env.getProperty("app.name"));
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
//        System.out.println("Startup time: " + formatter.format(new Date(ctx.getStartupDate())));
        return args -> {
           /* System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }*/
        };
    }
}
