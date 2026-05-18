package cn.edu.sdu.java.server.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelGenerator {

    /**
     * 生成学生成绩单 Excel
     * @param studentName 学生姓名
     * @param studentNum 学号
     * @param className 班级
     * @param scores 成绩列表
     * @return Excel 字节数组
     */
    public static byte[] generateScoreExcel(String studentName, String studentNum, String className, 
                                            List<Map<String, Object>> scores) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("成绩单");
            
            // 创建样式
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font titleFont = workbook.createFont();
            titleFont.setFontName("宋体");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            titleStyle.setFont(titleFont);
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            Font headerFont = workbook.createFont();
            headerFont.setFontName("宋体");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            Font cellFont = workbook.createFont();
            cellFont.setFontName("宋体");
            cellFont.setFontHeightInPoints((short) 10);
            cellStyle.setFont(cellFont);
            
            // 设置列宽
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 3000);
            sheet.setColumnWidth(2, 2500);
            
            // 标题行
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("学 生 成 绩 单");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
            
            // 学生信息
            Row infoRow1 = sheet.createRow(2);
            infoRow1.setHeightInPoints(20);
            Cell infoCell1 = infoRow1.createCell(0);
            infoCell1.setCellValue("姓名：" + studentName);
            infoCell1.setCellStyle(cellStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 2));
            
            Row infoRow2 = sheet.createRow(3);
            infoRow2.setHeightInPoints(20);
            Cell infoCell2 = infoRow2.createCell(0);
            infoCell2.setCellValue("学号：" + studentNum + "    班级：" + (className != null ? className : ""));
            infoCell2.setCellStyle(cellStyle);
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 2));
            
            // 表头
            Row headerRow = sheet.createRow(5);
            headerRow.setHeightInPoints(25);
            String[] headers = {"课程名称", "学分", "成绩"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 数据行
            double totalCredit = 0;
            double totalScore = 0;
            int rowNum = 6;
            
            for (Map<String, Object> score : scores) {
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(20);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(score.get("courseName").toString());
                cell0.setCellStyle(cellStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(Double.parseDouble(score.get("credit").toString()));
                cell1.setCellStyle(cellStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(Double.parseDouble(score.get("mark").toString()));
                cell2.setCellStyle(cellStyle);
                
                totalCredit += Double.parseDouble(score.get("credit").toString());
                totalScore += Double.parseDouble(score.get("mark").toString());
            }
            
            // 汇总行
            Row summaryRow1 = sheet.createRow(rowNum + 1);
            summaryRow1.setHeightInPoints(20);
            Cell summaryCell1 = summaryRow1.createCell(0);
            summaryCell1.setCellValue("总学分：" + totalCredit);
            summaryCell1.setCellStyle(cellStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum + 1, rowNum + 1, 0, 2));
            
            Row summaryRow2 = sheet.createRow(rowNum + 2);
            summaryRow2.setHeightInPoints(20);
            Cell summaryCell2 = summaryRow2.createCell(0);
            double avgScore = scores.isEmpty() ? 0 : totalScore / scores.size();
            summaryCell2.setCellValue(String.format("平均分：%.2f", avgScore));
            summaryCell2.setCellStyle(cellStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum + 2, rowNum + 2, 0, 2));
            
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("生成 Excel 失败", e);
            return new byte[0];
        }
    }
}
