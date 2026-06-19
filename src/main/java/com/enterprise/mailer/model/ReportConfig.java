package com.enterprise.mailer.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportConfig {
    private Long id;
    private String reportName;
    private String cronExpression;
    private Boolean active;
    private String querySql;
    private String formats; // CSV,XLSX,PDF
    private String recipients; // comma separated
    private String subject;
    private String emailBodyTemplate;
    private String parameters; // JSON string
    private String databaseName;
    private LocalDateTime lastRunTime;
    private String lastStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
