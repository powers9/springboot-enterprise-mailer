package com.enterprise.mailer.service.generator;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Component
public class CsvGenerator {

    public void generateCsv(ResultSet rs, File outputFile) throws Exception {
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Write headers
            String[] headers = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                headers[i - 1] = metaData.getColumnLabel(i);
            }
            writer.writeNext(headers);
            
            // Write data
            String[] row = new String[columnCount];
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    Object obj = rs.getObject(i);
                    row[i - 1] = obj != null ? obj.toString() : "";
                }
                writer.writeNext(row);
            }
        }
    }
}
