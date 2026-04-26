package com.enterprise.mailer.util;

import org.springframework.stereotype.Component;

@Component
public class QueryValidator {

    public void validateQuery(String querySql) {
        if (querySql == null || querySql.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }
        String upperQuery = querySql.trim().toUpperCase();
        if (!upperQuery.startsWith("SELECT")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed");
        }
        if (upperQuery.contains("DELETE") || upperQuery.contains("UPDATE") || 
            upperQuery.contains("DROP") || upperQuery.contains("INSERT") ||
            upperQuery.contains("ALTER") || upperQuery.contains("TRUNCATE")) {
            throw new IllegalArgumentException("Query contains forbidden keywords (DELETE, UPDATE, DROP, etc.)");
        }
    }
}
