package com.enterprise.mailer.repository;

import com.enterprise.mailer.model.ReportLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReportLogRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ReportLogRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<ReportLog> rowMapper = (rs, rowNum) -> ReportLog.builder()
            .id(rs.getLong("id"))
            .configId(rs.getLong("config_id"))
            .triggeredTime(rs.getObject("triggered_time", LocalDateTime.class))
            .executionDurationMs(rs.getLong("execution_duration_ms"))
            .status(rs.getString("status"))
            .errorMessage(rs.getString("error_message"))
            .generatedFormats(rs.getString("generated_formats"))
            .sentTo(rs.getString("sent_to"))
            .createdAt(rs.getObject("created_at", LocalDateTime.class))
            .build();

    public void save(ReportLog log) {
        String sql = "INSERT INTO report_log (config_id, triggered_time, execution_duration_ms, status, error_message, generated_formats, sent_to, created_at) " +
                "VALUES (:configId, :triggeredTime, :executionDurationMs, :status, :errorMessage, :generatedFormats, :sentTo, CURRENT_TIMESTAMP)";
        SqlParameterSource paramSource = new BeanPropertySqlParameterSource(log);
        namedParameterJdbcTemplate.update(sql, paramSource);
    }

    public List<ReportLog> findAll() {
        return jdbcTemplate.query("SELECT * FROM report_log ORDER BY triggered_time DESC", rowMapper);
    }

    public List<ReportLog> findByConfigId(Long configId) {
        return jdbcTemplate.query("SELECT * FROM report_log WHERE config_id = ? ORDER BY triggered_time DESC", rowMapper, configId);
    }
}
