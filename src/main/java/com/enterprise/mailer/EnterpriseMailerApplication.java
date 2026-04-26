package com.enterprise.mailer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableAsync
@EnableRetry
public class EnterpriseMailerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseMailerApplication.class, args);
    }

}
