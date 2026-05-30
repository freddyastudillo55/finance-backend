package finance.backend.bvnos.service;

import finance.backend.bvnos.model.PaymentRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExcelParserServiceImpl implements ExcelParserService {

    @Override
    public List<PaymentRecord> parseVesta(MultipartFile file) throws IOException {
        log.info("Parsing Vesta file");
        List<PaymentRecord> records = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String customerId = getStringValue(row.getCell(0));
                if (isSummaryRow(customerId)) {
                    continue;
                }

                BigDecimal amount = getBigDecimalValue(row.getCell(1));
                LocalDate paymentDate = getLocalDateValue(row.getCell(2));
                String service = getStringValue(row.getCell(3));

                PaymentRecord record = PaymentRecord.builder()
                        .customerId(customerId)
                        .amount(amount)
                        .paymentDate(paymentDate)
                        .service(service)
                        .source("VESTA")
                        .build();

                records.add(record);
            }
        }

        return records;
    }

    @Override
    public List<PaymentRecord> parseAax(MultipartFile file) throws IOException {
        log.info("Parsing AAX file");
        List<PaymentRecord> records = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String customerId = getStringValue(row.getCell(0));
                if (isSummaryRow(customerId)) {
                    continue;
                }

                BigDecimal amount = getBigDecimalValue(row.getCell(1));
                LocalDate paymentDate = getLocalDateValue(row.getCell(2));
                String service = getStringValue(row.getCell(3));

                PaymentRecord record = PaymentRecord.builder()
                        .customerId(customerId)
                        .amount(amount)
                        .paymentDate(paymentDate)
                        .service(service)
                        .source("AAX")
                        .build();

                records.add(record);
            }
        }

        return records;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue().trim();
            }
            if (cell.getCellType() == CellType.NUMERIC) {
                DataFormatter formatter = new DataFormatter();
                return formatter.formatCellValue(cell).trim();
            }
            if (cell.getCellType() == CellType.BOOLEAN) {
                return String.valueOf(cell.getBooleanCellValue());
            }
            if (cell.getCellType() == CellType.FORMULA) {
                try {
                    return String.valueOf((int) cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue().trim();
                }
            }
            return "";
        } catch (Exception e) {
            log.error("Error reading cell value at row {} col {}", cell.getRowIndex(), cell.getColumnIndex(), e);
            return "";
        }
    }

    private BigDecimal getBigDecimalValue(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue();
                value = value
                        .replace("$", "")
                        .replace(",", "")
                        .replace("USD", "")
                        .trim();
                if (value.isEmpty()) {
                    return BigDecimal.ZERO;
                }
                return new BigDecimal(value);
            }
            if (cell.getCellType() == CellType.FORMULA) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
        } catch (Exception e) {
            String cellRef = new CellReference(cell).formatAsString();
            throw new RuntimeException(
                    "Error parsing amount at cell " + cellRef + " (row " + (cell.getRowIndex() + 1) + "): " + e.getMessage(), e
            );
        }
        return BigDecimal.ZERO;
    }

    private LocalDate getLocalDateValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate();
                }
            }
            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isBlank()) {
                    return null;
                }
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                return LocalDate.parse(value, dateFormatter);
            }
            if (cell.getCellType() == CellType.FORMULA) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate();
                }
            }
        } catch (Exception e) {
            String cellRef = new CellReference(cell).formatAsString();
            throw new RuntimeException(
                    "Error parsing date at cell " + cellRef + " (row " + (cell.getRowIndex() + 1) + "): " + e.getMessage(), e
            );
        }
        return null;
    }

    private boolean isSummaryRow(String value) {
        if (value == null) {
            return true;
        }
        String normalized = value
                .trim()
                .toLowerCase();
        return normalized.equals("total")
                || normalized.contains("total")
                || normalized.contains("summary");
    }
}
