package com.enterprise.mailer.controller;

import com.enterprise.mailer.model.ReportConfig;
import com.enterprise.mailer.service.ReportConfigService;
import com.enterprise.mailer.service.ReportLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {

    private final ReportConfigService configService;
    private final ReportLogService logService;
    private final com.enterprise.mailer.service.MultiDatabaseService multiDatabaseService;

    public DashboardController(ReportConfigService configService, ReportLogService logService,
                               com.enterprise.mailer.service.MultiDatabaseService multiDatabaseService) {
        this.configService = configService;
        this.logService = logService;
        this.multiDatabaseService = multiDatabaseService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("configsCount", configService.getAllConfigs().size());
        model.addAttribute("logsCount", logService.getAllLogs().size());
        model.addAttribute("recentLogs", logService.getAllLogs().stream().limit(10).toArray());
        return "dashboard";
    }

    @GetMapping("/configs")
    public String configs(Model model) {
        model.addAttribute("configs", configService.getAllConfigs());
        return "configs";
    }

    @GetMapping("/configs/new")
    public String newConfigForm(Model model) {
        model.addAttribute("config", new ReportConfig());
        model.addAttribute("availableDbs", multiDatabaseService.getAvailableDatabaseNames());
        return "config-form";
    }

    @GetMapping("/configs/edit/{id}")
    public String editConfigForm(@PathVariable Long id, Model model) {
        model.addAttribute("config", configService.getConfigById(id));
        model.addAttribute("availableDbs", multiDatabaseService.getAvailableDatabaseNames());
        return "config-form";
    }

    @GetMapping("/logs")
    public String logs(Model model) {
        model.addAttribute("logs", logService.getAllLogs());
        return "logs";
    }

    @GetMapping("/query-tester")
    public String queryTester(Model model) {
        model.addAttribute("availableDbs", multiDatabaseService.getAvailableDatabaseNames());
        return "query-tester";
    }
}
