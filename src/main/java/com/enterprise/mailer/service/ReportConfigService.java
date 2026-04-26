package com.enterprise.mailer.service;

import com.enterprise.mailer.model.ReportConfig;
import com.enterprise.mailer.repository.ReportConfigRepository;
import com.enterprise.mailer.util.QueryValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportConfigService {

    private final ReportConfigRepository repository;
    private final QueryValidator queryValidator;

    public ReportConfigService(ReportConfigRepository repository, QueryValidator queryValidator) {
        this.repository = repository;
        this.queryValidator = queryValidator;
    }

    public List<ReportConfig> getAllConfigs() {
        return repository.findAll();
    }

    public List<ReportConfig> getActiveConfigs() {
        return repository.findAllActive();
    }

    public ReportConfig getConfigById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Config not found with id: " + id));
    }

    public ReportConfig saveConfig(ReportConfig config) {
        queryValidator.validateQuery(config.getQuerySql());
        return repository.save(config);
    }

    public void deleteConfig(Long id) {
        repository.deleteById(id);
    }
}
