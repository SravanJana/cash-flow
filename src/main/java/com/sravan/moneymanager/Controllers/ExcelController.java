package com.sravan.moneymanager.Controllers;

import com.sravan.moneymanager.DTO.ExpenseDTO;
import com.sravan.moneymanager.DTO.IncomeDTO;
import com.sravan.moneymanager.Entity.ProfileEntity;
import com.sravan.moneymanager.Service.EmailService;
import com.sravan.moneymanager.Service.ExpenseService;
import com.sravan.moneymanager.Service.IncomeService;
import com.sravan.moneymanager.Service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/excel")
public class ExcelController {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final EmailService emailService;
    private final ProfileService profileService;

    @GetMapping("/download/incomes")
    public ResponseEntity<byte[]> downloadCurrentMonthIncomesExcel() throws IOException {
        List<IncomeDTO> incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
        byte[] bytes = buildIncomeWorkbookBytes(incomes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=income-current-month.xlsx");

        return ResponseEntity.ok()
                             .headers(headers)
                             .contentLength(bytes.length)
                             .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                             .body(bytes);
    }

    @GetMapping("/email/incomes")
    public ResponseEntity<String> emailCurrentMonthIncomes() throws IOException {
        List<IncomeDTO> incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
        byte[] bytes = buildIncomeWorkbookBytes(incomes);

        ProfileEntity currentProfile = profileService.getCurrentProfile();
        String toEmail = currentProfile.getEmail();

        // Compose a friendly HTML body
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
        String monthTitle = LocalDate.now().format(monthFmt);

        BigDecimal total = incomes.stream()
                                  .map(inc -> inc.getAmount() == null ? BigDecimal.ZERO : inc.getAmount())
                                  .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family:Arial,Helvetica,sans-serif;color:#333;\">")
            .append("<h2 style=\"color:#2E7D32;\">Your Incomes for ").append(monthTitle).append("</h2>")
            .append("<p>Hello <strong>").append(currentProfile.getFullName() != null ? currentProfile.getFullName() : "there").append("</strong>,</p>")
            .append("<p>Attached is a friendly summary of your incomes for <strong>").append(monthTitle).append("</strong>. Below is a quick snapshot:</p>")
            .append("<ul>")
            .append("<li><strong>Total incomes:</strong> ").append(total.toString()).append("</li>")
            .append("<li><strong>Entries:</strong> ").append(incomes.size()).append("</li>")
            .append("</ul>")
            .append("<p style=\"margin-top:16px;\">If you have any questions, reply to this email and we'll help.</p>")
            .append("<p style=\"color:#777;font-size:12px;\">— MoneyManager</p>")
            .append("</body></html>");

        String subject = "Your Incomes Report — " + monthTitle;

        emailService.sendEmailWithAttachment(toEmail, subject, html.toString(), bytes, "income-current-month.xlsx");

        return ResponseEntity.ok("Excel sent to " + toEmail + " successfully.");
    }

    @GetMapping("/download/expenses")
    public ResponseEntity<byte[]> downloadCurrentMonthExpensesExcel() throws IOException {
        List<ExpenseDTO> expenses = expenseService.getCurrentMonthExpensesForCurrentUser();
        byte[] bytes = buildExpenseWorkbookBytes(expenses);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expense-current-month.xlsx");

        return ResponseEntity.ok()
                             .headers(headers)
                             .contentLength(bytes.length)
                             .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                             .body(bytes);
    }

    @GetMapping("/email/expenses")
    public ResponseEntity<String> emailCurrentMonthExpenses() throws IOException {
        List<ExpenseDTO> expenses = expenseService.getCurrentMonthExpensesForCurrentUser();
        byte[] bytes = buildExpenseWorkbookBytes(expenses);

        ProfileEntity currentProfile = profileService.getCurrentProfile();
        String toEmail = currentProfile.getEmail();

        // Compose a friendly HTML body for expenses
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
        String monthTitle = LocalDate.now().format(monthFmt);

        BigDecimal total = expenses.stream()
                                  .map(exp -> exp.getAmount() == null ? BigDecimal.ZERO : exp.getAmount())
                                  .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family:Arial,Helvetica,sans-serif;color:#333;\">")
            .append("<h2 style=\"color:#C62828;\">Your Expenses for ").append(monthTitle).append("</h2>")
            .append("<p>Hello <strong>").append(currentProfile.getFullName() != null ? currentProfile.getFullName() : "there").append("</strong>,</p>")
            .append("<p>Attached is a friendly summary of your expenses for <strong>").append(monthTitle).append("</strong>. Below is a quick snapshot:</p>")
            .append("<ul>")
            .append("<li><strong>Total expenses:</strong> ").append(total.toString()).append("</li>")
            .append("<li><strong>Entries:</strong> ").append(expenses.size()).append("</li>")
            .append("</ul>")
            .append("<p style=\"margin-top:16px;\">If you have any questions, reply to this email and we'll help.</p>")
            .append("<p style=\"color:#777;font-size:12px;\">— MoneyManager</p>")
            .append("</body></html>");

        String subject = "Your Expenses Report — " + monthTitle;

        emailService.sendEmailWithAttachment(toEmail, subject, html.toString(), bytes, "expense-current-month.xlsx");

        return ResponseEntity.ok("Excel sent to " + toEmail + " successfully.");
    }

    private byte[] buildExpenseWorkbookBytes(List<ExpenseDTO> expenses) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Current Month Expenses");

            // header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Amount");

            DateTimeFormatter df = DateTimeFormatter.ISO_DATE;

            int rowIdx = 1;
            for (ExpenseDTO inc : expenses) {
                Row row = sheet.createRow(rowIdx++);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(inc.getDate() != null ? inc.getDate().format(df) : "");

                row.createCell(1).setCellValue(inc.getName() != null ? inc.getName() : "");
                row.createCell(2).setCellValue(inc.getCategoryName() != null ? inc.getCategoryName() : "");
                row.createCell(3).setCellValue(inc.getAmount() != null ? inc.getAmount().toString() : "0");
            }

            // autosize columns
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] buildIncomeWorkbookBytes(List<IncomeDTO> incomes) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Current Month Incomes");

            // header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Amount");

            DateTimeFormatter df = DateTimeFormatter.ISO_DATE;

            int rowIdx = 1;
            for (IncomeDTO inc : incomes) {
                Row row = sheet.createRow(rowIdx++);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(inc.getDate() != null ? inc.getDate().format(df) : "");

                row.createCell(1).setCellValue(inc.getName() != null ? inc.getName() : "");
                row.createCell(2).setCellValue(inc.getCategoryName() != null ? inc.getCategoryName() : "");
                row.createCell(3).setCellValue(inc.getAmount() != null ? inc.getAmount().toString() : "0");
            }

            // autosize columns
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

}
