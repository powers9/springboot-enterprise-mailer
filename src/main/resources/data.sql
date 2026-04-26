INSERT INTO report_config (report_name, cron_expression, active, query_sql, formats, recipients, subject, email_body_template, parameters)
VALUES ('Daily System Report', '0 0/5 * * * ?', true, 'SELECT * FROM report_config', 'CSV,XLSX,PDF', 'admin@example.com', 'Daily Report', 'default-email', '{}');
