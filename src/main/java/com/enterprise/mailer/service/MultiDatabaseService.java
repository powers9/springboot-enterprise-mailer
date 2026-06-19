package com.enterprise.mailer.service;

import com.enterprise.mailer.config.DatabaseProperties;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.*;

@Service
@Slf4j
public class MultiDatabaseService {

    private final Map<String, NamedParameterJdbcTemplate> jdbcTemplates = new HashMap<>();
    private final Map<String, DataSource> dataSources = new HashMap<>();
    private final DatabaseProperties databaseProperties;
    private final DataSourceProperties defaultDataSourceProperties;
    private final NamedParameterJdbcTemplate defaultJdbcTemplate;

    public MultiDatabaseService(DatabaseProperties databaseProperties,
                                DataSourceProperties defaultDataSourceProperties,
                                NamedParameterJdbcTemplate defaultJdbcTemplate) {
        this.databaseProperties = databaseProperties;
        this.defaultDataSourceProperties = defaultDataSourceProperties;
        this.defaultJdbcTemplate = defaultJdbcTemplate;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing read-only datasources...");

        // Create read-only user on the default database
        try {
            defaultJdbcTemplate.getJdbcTemplate().execute("CREATE USER IF NOT EXISTS reader PASSWORD 'reader'");
            defaultJdbcTemplate.getJdbcTemplate().execute("GRANT SELECT ON SCHEMA PUBLIC TO reader");
            log.info("Created reader user on default database");
        } catch (Exception e) {
            log.warn("Could not create reader user on default database", e);
        }

        // Register default database as read-only datasource pointing to the same URL
        try {
            String url = cleanUrlForReader(defaultDataSourceProperties.getUrl());
            DataSource defaultReadOnlyDs = DataSourceBuilder.create()
                    .url(url)
                    .username("reader")
                    .password("reader")
                    .driverClassName(defaultDataSourceProperties.getDriverClassName())
                    .build();
            if (defaultReadOnlyDs instanceof HikariDataSource) {
                ((HikariDataSource) defaultReadOnlyDs).setReadOnly(true);
            }
            dataSources.put("default", defaultReadOnlyDs);
            jdbcTemplates.put("default", new NamedParameterJdbcTemplate(defaultReadOnlyDs));
            log.info("Registered default database as read-only via reader user (cleaned url: {})", url);
        } catch (Exception e) {
            log.error("Failed to register default database as read-only, falling back to writable connection", e);
            jdbcTemplates.put("default", defaultJdbcTemplate);
        }

        // Register other configured databases
        databaseProperties.getDatabases().forEach((name, config) -> {
            try {
                // Initialize sample schema/data using direct connection while writeable
                initializeTargetDatabase(name, config);

                String url = cleanUrlForReader(config.getUrl());
                DataSource ds = DataSourceBuilder.create()
                        .url(url)
                        .username("reader")
                        .password("reader")
                        .driverClassName(config.getDriverClassName())
                        .build();

                if (ds instanceof HikariDataSource) {
                    ((HikariDataSource) ds).setReadOnly(true);
                    log.info("DataSource {} configured as read-only via Hikari (cleaned url: {})", name, url);
                }
                dataSources.put(name, ds);
                jdbcTemplates.put(name, new NamedParameterJdbcTemplate(ds));
                log.info("Registered read-only datasource: {}", name);
            } catch (Exception e) {
                log.error("Failed to initialize datasource: {}", name, e);
            }
        });
    }

    private void initializeTargetDatabase(String name, DatabaseProperties.DatabaseConfig config) {
        try {
            if (config.getDriverClassName() != null) {
                Class.forName(config.getDriverClassName());
            }
        } catch (Exception e) {
            log.warn("Could not load driver class: {}", config.getDriverClassName(), e);
        }
        
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
             java.sql.Statement stmt = conn.createStatement()) {
            if ("sales_db".equals(name)) {
                stmt.execute("CREATE TABLE IF NOT EXISTS sales (id INT PRIMARY KEY, product VARCHAR(100), amount DECIMAL(10,2))");
                try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sales")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        stmt.execute("INSERT INTO sales VALUES (1, 'Laptop', 1200.00)");
                        stmt.execute("INSERT INTO sales VALUES (2, 'Phone', 800.00)");
                        stmt.execute("INSERT INTO sales VALUES (3, 'Tablet', 450.00)");
                        log.info("Initialized sales_db with sample data");
                    }
                }
            } else if ("hr_db".equals(name)) {
                stmt.execute("CREATE TABLE IF NOT EXISTS employees (id INT PRIMARY KEY, name VARCHAR(100), department VARCHAR(50))");
                try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM employees")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        stmt.execute("INSERT INTO employees VALUES (1, 'Alice Smith', 'HR')");
                        stmt.execute("INSERT INTO employees VALUES (2, 'Bob Jones', 'Engineering')");
                        stmt.execute("INSERT INTO employees VALUES (3, 'Charlie Brown', 'Marketing')");
                        log.info("Initialized hr_db with sample data");
                    }
                }
            }

            // Create read-only reader user for connection pooling
            stmt.execute("CREATE USER IF NOT EXISTS reader PASSWORD 'reader'");
            stmt.execute("GRANT SELECT ON SCHEMA PUBLIC TO reader");
            log.info("Created reader user on database: {}", name);
        } catch (Exception e) {
            log.error("Failed to initialize target database: {}", name, e);
        }
    }

    @PreDestroy
    public void close() {
        dataSources.values().forEach(ds -> {
            if (ds instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) ds).close();
                } catch (Exception e) {
                    log.error("Failed to close datasource", e);
                }
            }
        });
    }

    public List<String> getAvailableDatabaseNames() {
        List<String> names = new ArrayList<>(jdbcTemplates.keySet());
        Collections.sort(names);
        return names;
    }

    public NamedParameterJdbcTemplate getJdbcTemplate(String name) {
        return jdbcTemplates.get(name);
    }

    private String cleanUrlForReader(String url) {
        if (url == null) {
            return null;
        }
        // Remove H2 properties that require admin privileges (like DB_CLOSE_DELAY and DB_CLOSE_ON_EXIT)
        return url.replaceAll("(?i);DB_CLOSE_DELAY=[^;]*", "")
                  .replaceAll("(?i);DB_CLOSE_ON_EXIT=[^;]*", "");
    }
}
