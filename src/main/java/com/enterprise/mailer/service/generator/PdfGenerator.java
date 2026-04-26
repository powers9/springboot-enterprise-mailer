package com.enterprise.mailer.service.generator;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PdfGenerator {

    private final TemplateEngine templateEngine;
    private static final int MAX_PDF_ROWS = 5000;

    public PdfGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void generatePdf(ResultSet rs, File outputFile, String reportName) throws Exception {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        List<String> headers = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            headers.add(metaData.getColumnLabel(i));
        }
        
        List<Map<String, Object>> data = new ArrayList<>();
        int count = 0;
        while (rs.next() && count < MAX_PDF_ROWS) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(headers.get(i - 1), rs.getObject(i));
            }
            data.add(row);
            count++;
        }
        
        Context context = new Context();
        context.setVariable("headers", headers);
        context.setVariable("data", data);
        context.setVariable("reportName", reportName);
        
        String htmlContent = templateEngine.process("pdf-template", context);
        
        try (FileOutputStream os = new FileOutputStream(outputFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(os);
        }
    }
}
