package com.enterprise.mailer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class DatabaseProperties {
    private Map<String, DatabaseConfig> databases = new HashMap<>();

    @Data
    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
    }
}
