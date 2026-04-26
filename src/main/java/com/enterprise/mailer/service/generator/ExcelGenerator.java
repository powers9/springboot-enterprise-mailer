package com.enterprise.mailer.service.generator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

@Component
public class ExcelGenerator {

    public void generateExcel(ResultSet rs, File outputFile) throws Exception {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) { // keep 100 rows in memory
            Sheet sheet = workbook.createSheet("Report Data");
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Write headers
            Row headerRow = sheet.createRow(0);
            for (int i = 1; i <= columnCount; i++) {
                Cell cell = headerRow.createCell(i - 1);
                cell.setCellValue(metaData.getColumnLabel(i));
            }
            
            // Write data
            int rowIndex = 1;
            while (rs.next()) {
                Row dataRow = sheet.createRow(rowIndex++);
                for (int i = 1; i <= columnCount; i++) {
                    Cell cell = dataRow.createCell(i - 1);
                    Object obj = rs.getObject(i);
                    if (obj != null) {
                        if (obj instanceof Number) {
                            cell.setCellValue(((Number) obj).doubleValue());
                        } else {
                            cell.setCellValue(obj.toString());
                        }
                    }
                }
            }
            
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                workbook.write(out);
            }
            // Dispose of temporary files backing this workbook on disk
            workbook.dispose();
        }
    }
}
