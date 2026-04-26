package com.enterprise.mailer.service;

import com.enterprise.mailer.model.ReportLog;
import com.enterprise.mailer.repository.ReportLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportLogService {

    private final ReportLogRepository repository;

    public ReportLogService(ReportLogRepository repository) {
        this.repository = repository;
    }

    public void saveLog(ReportLog log) {
        repository.save(log);
    }

    public List<ReportLog> getAllLogs() {
        return repository.findAll();
    }
}
