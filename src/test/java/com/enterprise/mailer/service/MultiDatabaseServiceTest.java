package com.enterprise.mailer.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MultiDatabaseServiceTest {

    @Autowired
    private MultiDatabaseService multiDatabaseService;

    @Test
    public void testAvailableDatabases() {
        List<String> dbNames = multiDatabaseService.getAvailableDatabaseNames();
        assertTrue(dbNames.contains("default"));
        assertTrue(dbNames.contains("sales_db"));
        assertTrue(dbNames.contains("hr_db"));
    }

    @Test
    public void testSalesDbInitializationAndReadOnly() {
        NamedParameterJdbcTemplate salesTemplate = multiDatabaseService.getJdbcTemplate("sales_db");
        assertNotNull(salesTemplate);

        // Test querying
        List<Map<String, Object>> results = salesTemplate.queryForList("SELECT * FROM sales", Map.of());
        assertFalse(results.isEmpty());
        assertEquals("Laptop", results.get(0).get("PRODUCT"));

        // Test read-only constraint: inserting should throw exception
        assertThrows(Exception.class, () -> {
            salesTemplate.update("INSERT INTO sales VALUES (99, 'Tablet', 199.99)", Map.of());
        });
    }

    @Test
    public void testHrDbInitializationAndReadOnly() {
        NamedParameterJdbcTemplate hrTemplate = multiDatabaseService.getJdbcTemplate("hr_db");
        assertNotNull(hrTemplate);

        // Test querying
        List<Map<String, Object>> results = hrTemplate.queryForList("SELECT * FROM employees", Map.of());
        assertFalse(results.isEmpty());
        assertEquals("Alice Smith", results.get(0).get("NAME"));

        // Test read-only constraint: inserting should throw exception
        assertThrows(Exception.class, () -> {
            hrTemplate.update("INSERT INTO employees VALUES (99, 'Dave', 'Finance')", Map.of());
        });
    }
}
