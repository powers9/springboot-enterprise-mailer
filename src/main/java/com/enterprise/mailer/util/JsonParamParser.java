package com.enterprise.mailer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JsonParamParser {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> parseParameters(String jsonParams) {
        if (jsonParams == null || jsonParams.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonParams, new TypeReference<Map<String, Object>>(){});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON parameters: {}", jsonParams, e);
            throw new IllegalArgumentException("Invalid JSON parameters format", e);
        }
    }
}
