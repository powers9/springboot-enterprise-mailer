CREATE TABLE IF NOT EXISTS report_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_name VARCHAR(255) NOT NULL,
    cron_expression VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    query_sql TEXT NOT NULL,
    formats VARCHAR(255),
    recipients VARCHAR(1000),
    subject VARCHAR(255),
    email_body_template VARCHAR(255),
    parameters TEXT,
    last_run_time TIMESTAMP,
    last_status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS report_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_id BIGINT NOT NULL,
    triggered_time TIMESTAMP NOT NULL,
    execution_duration_ms BIGINT,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    generated_formats VARCHAR(255),
    sent_to VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (config_id) REFERENCES report_config(id) ON DELETE CASCADE
);
