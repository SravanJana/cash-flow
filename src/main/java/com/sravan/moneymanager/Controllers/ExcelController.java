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
        html.append("<!DOCTYPE html>")
                .append("<html lang='en'>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("</head>")
                .append("<body style='margin: 0; padding: 0; background-color: #0a0a0a; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif;'>")
                .append("<table width='100%' cellpadding='0' cellspacing='0' style='background-color: #0a0a0a; padding: 40px 20px;'>")
                .append("<tr><td align='center'>")
                .append("<table width='600' cellpadding='0' cellspacing='0' style='background-color: #1a1a1a; border-radius: 12px; border: 1px solid #2a2a2a; overflow: hidden;'>")
                .append("<!-- Header -->")
                .append("<tr><td style='background: linear-gradient(135deg, #10b981 0%, #8b5cf6 100%); padding: 40px 30px; text-align: center;'>")
                .append("<h1 style='margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;'>💰 Income Report</h1>")
                .append("<p style='margin: 10px 0 0 0; color: #d1fae5; font-size: 14px;'>").append(monthTitle).append("</p>")
                .append("</td></tr>")
                .append("<!-- Content -->")
                .append("<tr><td style='padding: 40px;'>")
                .append("<h2 style='margin: 0 0 20px 0; color: #e0e0e0; font-size: 20px; font-weight: 500;'>Hi ").append(currentProfile.getFullName() != null ? currentProfile.getFullName() : "there").append("! 👋</h2>")
                .append("<p style='margin: 0 0 25px 0; color: #b0b0b0; font-size: 15px; line-height: 1.6;'>Attached is your complete income summary for <strong style='color: #e0e0e0;'>").append(monthTitle).append("</strong>. Here's a quick overview:</p>")
                .append("<!-- Summary Box -->")
                .append("<div style='background-color: #242424; border-radius: 8px; padding: 20px; margin: 20px 0;'>")
                .append("<table width='100%' cellpadding='0' cellspacing='0'>")
                .append("<tr>")
                .append("<td style='padding: 10px 0;'>")
                .append("<p style='margin: 0; color: #888888; font-size: 13px;'>Total Income</p>")
                .append("<p style='margin: 5px 0 0 0; color: #51cf66; font-size: 24px; font-weight: 700;'>₹").append(total.toString()).append("</p>")
                .append("</td>")
                .append("<td style='padding: 10px 0; text-align: right;'>")
                .append("<p style='margin: 0; color: #888888; font-size: 13px;'>Total Entries</p>")
                .append("<p style='margin: 5px 0 0 0; color: #e0e0e0; font-size: 24px; font-weight: 700;'>").append(incomes.size()).append("</p>")
                .append("</td>")
                .append("</tr>")
                .append("</table>")
                .append("</div>")
                .append("<div style='background-color: #242424; border-left: 3px solid #51cf66; padding: 18px; margin: 25px 0; border-radius: 6px;'>")
                .append("<p style='margin: 0; color: #c0c0c0; font-size: 14px; line-height: 1.6;'>📎 The complete Excel report is attached to this email for your records.</p>")
                .append("</div>")
                .append("<p style='margin: 25px 0 0 0; color: #888888; font-size: 14px; line-height: 1.6;'>If you have any questions, feel free to reply to this email.</p>")
                .append("</td></tr>")
                .append("<!-- Footer -->")
                .append("<tr><td style='background-color: #242424; padding: 25px 40px; text-align: center; border-top: 1px solid #3a3a3a;'>")
                .append("<p style='margin: 0; color: #888888; font-size: 13px;'>Best regards,<br/><strong style='color: #a0a0a0;'>The Money Manager Team</strong></p>")
                .append("</td></tr>")
                .append("</table>")
                .append("</td></tr></table>")
                .append("</body>")
                .append("</html>");

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
        html.append("<!DOCTYPE html>")
                .append("<html lang='en'>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("</head>")
                .append("<body style='margin: 0; padding: 0; background-color: #0a0a0a; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif;'>")
                .append("<table width='100%' cellpadding='0' cellspacing='0' style='background-color: #0a0a0a; padding: 40px 20px;'>")
                .append("<tr><td align='center'>")
                .append("<table width='600' cellpadding='0' cellspacing='0' style='background-color: #1a1a1a; border-radius: 12px; border: 1px solid #2a2a2a; overflow: hidden;'>")
                .append("<!-- Header -->")
                .append("<tr><td style='background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%); padding: 40px 30px; text-align: center;'>")
                .append("<h1 style='margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;'>📊 Expense Report</h1>")
                .append("<p style='margin: 10px 0 0 0; color: #fce7f3; font-size: 14px;'>").append(monthTitle).append("</p>")
                .append("</td></tr>")
                .append("<!-- Content -->")
                .append("<tr><td style='padding: 40px;'>")
                .append("<h2 style='margin: 0 0 20px 0; color: #e0e0e0; font-size: 20px; font-weight: 500;'>Hi ").append(currentProfile.getFullName() != null ? currentProfile.getFullName() : "there").append("! 👋</h2>")
                .append("<p style='margin: 0 0 25px 0; color: #b0b0b0; font-size: 15px; line-height: 1.6;'>Attached is your complete expense summary for <strong style='color: #e0e0e0;'>").append(monthTitle).append("</strong>. Here's a quick overview:</p>")
                .append("<!-- Summary Box -->")
                .append("<div style='background-color: #242424; border-radius: 8px; padding: 20px; margin: 20px 0;'>")
                .append("<table width='100%' cellpadding='0' cellspacing='0'>")
                .append("<tr>")
                .append("<td style='padding: 10px 0;'>")
                .append("<p style='margin: 0; color: #888888; font-size: 13px;'>Total Expenses</p>")
                .append("<p style='margin: 5px 0 0 0; color: #ff6b6b; font-size: 24px; font-weight: 700;'>₹").append(total.toString()).append("</p>")
                .append("</td>")
                .append("<td style='padding: 10px 0; text-align: right;'>")
                .append("<p style='margin: 0; color: #888888; font-size: 13px;'>Total Entries</p>")
                .append("<p style='margin: 5px 0 0 0; color: #e0e0e0; font-size: 24px; font-weight: 700;'>").append(expenses.size()).append("</p>")
                .append("</td>")
                .append("</tr>")
                .append("</table>")
                .append("</div>")
                .append("<div style='background-color: #242424; border-left: 3px solid #ff6b6b; padding: 18px; margin: 25px 0; border-radius: 6px;'>")
                .append("<p style='margin: 0; color: #c0c0c0; font-size: 14px; line-height: 1.6;'>📎 The complete Excel report is attached to this email for your records.</p>")
                .append("</div>")
                .append("<p style='margin: 25px 0 0 0; color: #888888; font-size: 14px; line-height: 1.6;'>If you have any questions, feel free to reply to this email.</p>")
                .append("</td></tr>")
                .append("<!-- Footer -->")
                .append("<tr><td style='background-color: #242424; padding: 25px 40px; text-align: center; border-top: 1px solid #3a3a3a;'>")
                .append("<p style='margin: 0; color: #888888; font-size: 13px;'>Best regards,<br/><strong style='color: #a0a0a0;'>The Money Manager Team</strong></p>")
                .append("</td></tr>")
                .append("</table>")
                .append("</td></tr></table>")
                .append("</body>")
                .append("</html>");

        String subject = "Your Expenses Report — " + monthTitle;

        emailService.sendEmailWithAttachment(toEmail, subject, html.toString(), bytes, "expense-current-month.xlsx");

        return ResponseEntity.ok("Excel sent to " + toEmail + " successfully.");
    }

    private byte[] buildExpenseWorkbookBytes(List<ExpenseDTO> expenses) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

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
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

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
