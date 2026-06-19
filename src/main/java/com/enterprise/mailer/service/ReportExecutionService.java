package com.enterprise.mailer.service;

import com.enterprise.mailer.model.ReportConfig;
import com.enterprise.mailer.model.ReportLog;
import com.enterprise.mailer.repository.ReportConfigRepository;
import com.enterprise.mailer.service.generator.CsvGenerator;
import com.enterprise.mailer.service.generator.ExcelGenerator;
import com.enterprise.mailer.service.generator.PdfGenerator;
import com.enterprise.mailer.util.FilenameGenerator;
import com.enterprise.mailer.util.JsonParamParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportExecutionService {

    private final ReportConfigRepository configRepository;
    private final ReportLogService logService;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MultiDatabaseService multiDatabaseService;
    private final JsonParamParser paramParser;
    private final CsvGenerator csvGenerator;
    private final ExcelGenerator excelGenerator;
    private final PdfGenerator pdfGenerator;
    private final EmailService emailService;
    private final FilenameGenerator filenameGenerator;

    @Value("${app.mailer.max-attachment-size-mb:20}")
    private long maxAttachmentSizeMb;

    public ReportExecutionService(ReportConfigRepository configRepository, ReportLogService logService,
                                  NamedParameterJdbcTemplate jdbcTemplate, MultiDatabaseService multiDatabaseService,
                                  JsonParamParser paramParser, CsvGenerator csvGenerator, ExcelGenerator excelGenerator,
                                  PdfGenerator pdfGenerator, EmailService emailService,
                                  FilenameGenerator filenameGenerator) {
        this.configRepository = configRepository;
        this.logService = logService;
        this.jdbcTemplate = jdbcTemplate;
        this.multiDatabaseService = multiDatabaseService;
        this.paramParser = paramParser;
        this.csvGenerator = csvGenerator;
        this.excelGenerator = excelGenerator;
        this.pdfGenerator = pdfGenerator;
        this.emailService = emailService;
        this.filenameGenerator = filenameGenerator;
    }

    @Async("reportTaskExecutor")
    public void executeReport(Long configId) {
        log.info("Starting execution for report config id: {}", configId);
        LocalDateTime startTime = LocalDateTime.now();
        ReportConfig config = configRepository.findById(configId).orElse(null);
        
        if (config == null) {
            log.error("Report config id {} not found.", configId);
            return;
        }

        ReportLog reportLog = ReportLog.builder()
                .configId(configId)
                .triggeredTime(startTime)
                .build();

        List<File> generatedFiles = new ArrayList<>();
        try {
            Map<String, Object> params = paramParser.parseParameters(config.getParameters());
            List<String> formats = Arrays.stream(config.getFormats().split(","))
                                         .map(String::trim)
                                         .map(String::toUpperCase)
                                         .collect(Collectors.toList());
            
            // Create temp dir with unique name to prevent collisions during concurrent runs
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "mailer_" + java.util.UUID.randomUUID());
            tempDir.mkdirs();

            String dbName = config.getDatabaseName();
            if (dbName == null || dbName.trim().isEmpty()) {
                dbName = "default";
            }
            NamedParameterJdbcTemplate targetTemplate = multiDatabaseService.getJdbcTemplate(dbName);
            if (targetTemplate == null) {
                throw new IllegalArgumentException("Database config '" + dbName + "' not found.");
            }

            for (String format : formats) {
                File outputFile = new File(tempDir, filenameGenerator.generateFilename(config.getReportName(), format.toLowerCase()));
                log.info("Generating format {} for config id {} on database {}", format, configId, dbName);
                
                targetTemplate.query(config.getQuerySql(), params, rs -> {
                    try {
                        if ("CSV".equals(format)) {
                            csvGenerator.generateCsv(rs, outputFile);
                        } else if ("XLSX".equals(format)) {
                            excelGenerator.generateExcel(rs, outputFile);
                        } else if ("PDF".equals(format)) {
                            pdfGenerator.generatePdf(rs, outputFile, config.getReportName());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to generate " + format, e);
                    }
                    return null;
                });
                
                if (outputFile.exists()) {
                    generatedFiles.add(outputFile);
                }
            }

            // Check file sizes
            long totalSizeMb = generatedFiles.stream().mapToLong(File::length).sum() / (1024 * 1024);
            if (totalSizeMb > maxAttachmentSizeMb) {
                throw new IllegalStateException("Generated attachments exceed max size of " + maxAttachmentSizeMb + "MB. Actual: " + totalSizeMb + "MB");
            }

            // Send Email
            Context context = new Context();
            context.setVariable("reportName", config.getReportName());
            context.setVariable("executionTime", startTime);
            
            String templateName = config.getEmailBodyTemplate() != null && !config.getEmailBodyTemplate().isEmpty() ? 
                                  config.getEmailBodyTemplate() : "default-email";
                                  
            emailService.sendEmailWithAttachments(config.getRecipients(), config.getSubject(), templateName, context, generatedFiles);

            // Success
            reportLog.setStatus("SUCCESS");
            reportLog.setGeneratedFormats(config.getFormats());
            reportLog.setSentTo(config.getRecipients());
            
            configRepository.updateLastRunStatus(config.getId(), startTime, "SUCCESS");

        } catch (Exception e) {
            log.error("Failed to execute report config id: {}", configId, e);
            reportLog.setStatus("FAILED");
            reportLog.setErrorMessage(e.getMessage());
            configRepository.updateLastRunStatus(config.getId(), startTime, "FAILED");
        } finally {
            long duration = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
            reportLog.setExecutionDurationMs(duration);
            logService.saveLog(reportLog);
            
            // Cleanup temp files
            for (File file : generatedFiles) {
                if (file.exists()) {
                    file.delete();
                }
            }
            if (generatedFiles.size() > 0) {
                generatedFiles.get(0).getParentFile().delete();
            }
        }
    }
}
