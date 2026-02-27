package com.systemtmc.inventory.service;

import com.systemtmc.inventory.dto.MaterialMovementDTO;
import com.systemtmc.inventory.dto.RequestDTO;
import com.systemtmc.inventory.model.entity.MaterialMovement;
import com.systemtmc.inventory.model.entity.Request;
import com.systemtmc.inventory.model.enums.RequestStatus;
import com.systemtmc.inventory.repository.MaterialMovementRepository;
import com.systemtmc.inventory.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис для формирования отчетов
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final RequestRepository requestRepository;
    private final MaterialMovementRepository movementRepository;
    private final RequestService requestService;
    private final MaterialMovementService movementService;
    
    /**
     * Отчет по заявкам за период
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateRequestsReport(LocalDateTime startDate, LocalDateTime endDate, Long departmentId) {
        List<Request> requests;
        
        if (departmentId != null) {
            requests = requestRepository.findRequestsByDepartmentAndDateRange(departmentId, startDate, endDate);
        } else {
            requests = requestRepository.findRequestsByDateRange(startDate, endDate);
        }
        
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalRequests", requests.size());
        
        // Статистика по статусам
        Map<RequestStatus, Long> statusStats = requests.stream()
                .collect(Collectors.groupingBy(Request::getStatus, Collectors.counting()));
        report.put("statusStatistics", statusStats);
        
        // Статистика по подразделениям
        Map<String, Long> departmentStats = requests.stream()
                .collect(Collectors.groupingBy(r -> r.getDepartment().getName(), Collectors.counting()));
        report.put("departmentStatistics", departmentStats);
        
        return report;
    }
    
    /**
     * Отчет по расходу материалов за период
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateMaterialConsumptionReport(LocalDateTime startDate, LocalDateTime endDate, Long departmentId) {
        List<MaterialMovement> movements;
        
        if (departmentId != null) {
            movements = movementRepository.findMovementsByDepartmentAndDateRange(departmentId, startDate, endDate);
        } else {
            movements = movementRepository.findMovementsByDateRange(startDate, endDate);
        }
        
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalMovements", movements.size());
        
        // Статистика по типам движений
        Map<String, Long> typeStats = movements.stream()
                .collect(Collectors.groupingBy(m -> m.getMovementType().getDisplayName(), Collectors.counting()));
        report.put("movementTypeStatistics", typeStats);
        
        // Общая стоимость
        BigDecimal totalCost = movements.stream()
                .map(MaterialMovement::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalCost", totalCost);
        
        return report;
    }
    
    /**
     * Экспорт отчета по заявкам в Excel
     */
    public byte[] exportRequestsReportToExcel(LocalDateTime startDate, LocalDateTime endDate, Long departmentId) throws IOException {
        List<RequestDTO> requests;
        
        if (departmentId != null) {
            requests = requestService.getRequestsByDateRange(startDate, endDate).stream()
                    .filter(r -> r.getDepartmentId().equals(departmentId))
                    .collect(Collectors.toList());
        } else {
            requests = requestService.getRequestsByDateRange(startDate, endDate);
        }
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Отчет по заявкам");
            
            // Стиль заголовка
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Заголовок отчета
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Отчет по заявкам за период");
            titleCell.setCellStyle(headerStyle);
            
            Row periodRow = sheet.createRow(1);
            periodRow.createCell(0).setCellValue("Период: " + 
                    formatDateTime(startDate) + " - " + formatDateTime(endDate));
            
            // Пропуск строки
            int rowNum = 3;
            
            // Заголовки таблицы
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"№ заявки", "Дата", "Инициатор", "Подразделение", "Назначение", "Статус", "Кол-во позиций"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Данные
            for (RequestDTO request : requests) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(request.getRequestNumber());
                row.createCell(1).setCellValue(formatDateTime(request.getCreatedAt()));
                row.createCell(2).setCellValue(request.getRequesterName());
                row.createCell(3).setCellValue(request.getDepartmentName());
                row.createCell(4).setCellValue(request.getPurpose());
                row.createCell(5).setCellValue(request.getStatus().getDisplayName());
                row.createCell(6).setCellValue(request.getItems().size());
            }
            
            // Автоматическая ширина столбцов
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Запись в ByteArray
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * Экспорт отчета по расходу материалов в Excel
     */
    public byte[] exportMaterialConsumptionReportToExcel(LocalDateTime startDate, LocalDateTime endDate, Long departmentId) throws IOException {
        List<MaterialMovementDTO> movements;
        
        if (departmentId != null) {
            movements = movementService.getMovementsByDepartmentAndDateRange(departmentId, startDate, endDate);
        } else {
            movements = movementService.getMovementsByDateRange(startDate, endDate);
        }
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Отчет по расходу материалов");
            
            // Стиль заголовка
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Заголовок отчета
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Отчет по расходу материалов за период");
            titleCell.setCellStyle(headerStyle);
            
            Row periodRow = sheet.createRow(1);
            periodRow.createCell(0).setCellValue("Период: " + 
                    formatDateTime(startDate) + " - " + formatDateTime(endDate));
            
            // Пропуск строки
            int rowNum = 3;
            
            // Заголовки таблицы
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Дата", "Артикул", "Материал", "Тип движения", "Количество", "Остаток до", "Остаток после", "Подразделение"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Данные
            for (MaterialMovementDTO movement : movements) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(formatDateTime(movement.getCreatedAt()));
                row.createCell(1).setCellValue(movement.getMaterialArticle());
                row.createCell(2).setCellValue(movement.getMaterialName());
                row.createCell(3).setCellValue(movement.getMovementType().getDisplayName());
                row.createCell(4).setCellValue(movement.getQuantity().doubleValue());
                row.createCell(5).setCellValue(movement.getQuantityBefore().doubleValue());
                row.createCell(6).setCellValue(movement.getQuantityAfter().doubleValue());
                row.createCell(7).setCellValue(movement.getDepartmentName() != null ? movement.getDepartmentName() : "-");
            }
            
            // Автоматическая ширина столбцов
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Запись в ByteArray
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}
