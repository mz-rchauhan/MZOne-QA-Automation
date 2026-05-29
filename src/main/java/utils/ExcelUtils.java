package utils;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel Data Parser utility ported securely from modpr.
 */
public class ExcelUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);

    public List<HashMap<String, String>> readData(String filepath, String sheetName) {
        List<HashMap<String, String>> dataList = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        
        try (FileInputStream fs = new FileInputStream(filepath);
             XSSFWorkbook workbook = new XSSFWorkbook(fs)) {
             
            XSSFSheet sheet = workbook.getSheet(sheetName);
            if(sheet == null) {
                LOGGER.error("Sheet {} not found in {}", sheetName, filepath);
                return dataList;
            }
            
            XSSFRow headerRow = sheet.getRow(0);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                XSSFRow currentRow = sheet.getRow(i);
                if(currentRow == null) continue;
                
                HashMap<String, String> rowData = new HashMap<>();
                for (int j = 0; j < currentRow.getPhysicalNumberOfCells(); j++) {
                    if(headerRow.getCell(j) == null) continue;
                    
                    String key = headerRow.getCell(j).getStringCellValue();
                    String value = formatter.formatCellValue(currentRow.getCell(j));
                    rowData.put(key, value);
                }
                dataList.add(rowData);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read Excel data", e);
        }
        return dataList;
    }

    public static List<Map<String, String>> loadData(String filepath, String sheetName) {
        return new ArrayList<>(new ExcelUtils().readData(filepath, sheetName));
    }

    public static HashMap<String, String> readSoapCredentials(String filepath, String sheetName) {
        HashMap<String, String> credentials = new HashMap<>();
        try (FileInputStream fs = new FileInputStream(filepath);
             XSSFWorkbook workbook = new XSSFWorkbook(fs)) {
             
            XSSFSheet sheet = workbook.getSheet(sheetName);
            XSSFRow headerRow = sheet.getRow(0);
            
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                XSSFRow currentRow = sheet.getRow(i);
                if(currentRow != null) {
                    for (int j = 0; j < currentRow.getPhysicalNumberOfCells(); j++) {
                        XSSFCell headerCell = headerRow.getCell(j);
                        XSSFCell dataCell = currentRow.getCell(j);
                        if(headerCell != null && dataCell != null) {
                            credentials.put(headerCell.getStringCellValue(), dataCell.getStringCellValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read SOAP credentials", e);
        }
        return credentials;
    }
}
