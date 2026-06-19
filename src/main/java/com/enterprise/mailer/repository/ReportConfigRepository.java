package com.enterprise.mailer.repository;

import com.enterprise.mailer.model.ReportConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReportConfigRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ReportConfigRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<ReportConfig> rowMapper = (rs, rowNum) -> ReportConfig.builder()
            .id(rs.getLong("id"))
            .reportName(rs.getString("report_name"))
            .cronExpression(rs.getString("cron_expression"))
            .active(rs.getBoolean("active"))
            .querySql(rs.getString("query_sql"))
            .formats(rs.getString("formats"))
            .recipients(rs.getString("recipients"))
            .subject(rs.getString("subject"))
            .emailBodyTemplate(rs.getString("email_body_template"))
            .parameters(rs.getString("parameters"))
            .databaseName(rs.getString("database_name"))
            .lastRunTime(rs.getObject("last_run_time", LocalDateTime.class))
            .lastStatus(rs.getString("last_status"))
            .createdAt(rs.getObject("created_at", LocalDateTime.class))
            .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
            .createdBy(rs.getString("created_by"))
            .updatedBy(rs.getString("updated_by"))
            .build();

    public List<ReportConfig> findAll() {
        return jdbcTemplate.query("SELECT * FROM report_config ORDER BY id DESC", rowMapper);
    }

    public List<ReportConfig> findAllActive() {
        return jdbcTemplate.query("SELECT * FROM report_config WHERE active = TRUE", rowMapper);
    }

    public Optional<ReportConfig> findById(Long id) {
        List<ReportConfig> configs = jdbcTemplate.query("SELECT * FROM report_config WHERE id = ?", rowMapper, id);
        return configs.isEmpty() ? Optional.empty() : Optional.of(configs.get(0));
    }

    public ReportConfig save(ReportConfig config) {
        if (config.getId() == null) {
            String sql = "INSERT INTO report_config (report_name, cron_expression, active, query_sql, formats, recipients, subject, email_body_template, parameters, database_name, created_at, updated_at) " +
                    "VALUES (:reportName, :cronExpression, :active, :querySql, :formats, :recipients, :subject, :emailBodyTemplate, :parameters, :databaseName, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource paramSource = new BeanPropertySqlParameterSource(config);
            namedParameterJdbcTemplate.update(sql, paramSource, keyHolder, new String[]{"id"});
            config.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE report_config SET report_name = :reportName, cron_expression = :cronExpression, active = :active, " +
                    "query_sql = :querySql, formats = :formats, recipients = :recipients, subject = :subject, " +
                    "email_body_template = :emailBodyTemplate, parameters = :parameters, database_name = :databaseName, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = :id";
            SqlParameterSource paramSource = new BeanPropertySqlParameterSource(config);
            namedParameterJdbcTemplate.update(sql, paramSource);
        }
        return config;
    }

    public void updateLastRunStatus(Long id, LocalDateTime lastRunTime, String lastStatus) {
        jdbcTemplate.update("UPDATE report_config SET last_run_time = ?, last_status = ? WHERE id = ?", lastRunTime, lastStatus, id);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM report_config WHERE id = ?", id);
    }
}
