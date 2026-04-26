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
public class ReportLog {
    private Long id;
    private Long configId;
    private LocalDateTime triggeredTime;
    private Long executionDurationMs;
    private String status;
    private String errorMessage;
    private String generatedFormats;
    private String sentTo;
    private LocalDateTime createdAt;
}
