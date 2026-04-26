package com.enterprise.mailer.controller;

import com.enterprise.mailer.model.ReportConfig;
import com.enterprise.mailer.service.DynamicSchedulerService;
import com.enterprise.mailer.service.ReportConfigService;
import com.enterprise.mailer.service.ReportExecutionService;
import com.enterprise.mailer.util.JsonParamParser;
import com.enterprise.mailer.util.QueryValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ReportConfigService configService;
    private final DynamicSchedulerService schedulerService;
    private final ReportExecutionService executionService;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final QueryValidator queryValidator;
    private final JsonParamParser paramParser;

    public ApiController(ReportConfigService configService, DynamicSchedulerService schedulerService,
                         ReportExecutionService executionService, NamedParameterJdbcTemplate jdbcTemplate,
                         QueryValidator queryValidator, JsonParamParser paramParser) {
        this.configService = configService;
        this.schedulerService = schedulerService;
        this.executionService = executionService;
        this.jdbcTemplate = jdbcTemplate;
        this.queryValidator = queryValidator;
        this.paramParser = paramParser;
    }

    @PostMapping("/configs")
    public RedirectView saveConfig(@ModelAttribute ReportConfig config) {
        if (config.getActive() == null) {
            config.setActive(false);
        }
        ReportConfig saved = configService.saveConfig(config);
        schedulerService.scheduleTask(saved);
        return new RedirectView("/configs");
    }

    @PostMapping("/configs/delete/{id}")
    public RedirectView deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        schedulerService.cancelTask(id);
        return new RedirectView("/configs");
    }

    @PostMapping("/configs/trigger/{id}")
    public RedirectView triggerConfig(@PathVariable Long id) {
        executionService.executeReport(id);
        return new RedirectView("/configs");
    }

    @PostMapping("/configs/toggle/{id}")
    public RedirectView toggleConfig(@PathVariable Long id) {
        ReportConfig config = configService.getConfigById(id);
        config.setActive(!config.getActive());
        configService.saveConfig(config);
        schedulerService.scheduleTask(config);
        return new RedirectView("/configs");
    }

    @PostMapping("/test-query")
    public ResponseEntity<?> testQuery(@RequestBody Map<String, String> payload) {
        try {
            String sql = payload.get("query");
            String paramsStr = payload.get("parameters");
            
            queryValidator.validateQuery(sql);
            Map<String, Object> params = paramParser.parseParameters(paramsStr);
            
            // Limit to 100 rows for preview. Depending on dialect, we append limit.
            // A quick hack for H2/Oracle preview is just setting max rows in JdbcTemplate
            jdbcTemplate.getJdbcTemplate().setMaxRows(100);
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params);
            jdbcTemplate.getJdbcTemplate().setMaxRows(-1); // reset
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
