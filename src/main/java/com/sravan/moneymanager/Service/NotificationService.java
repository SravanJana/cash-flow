package com.sravan.moneymanager.Service;

import com.sravan.moneymanager.DTO.ExpenseDTO;
import com.sravan.moneymanager.Entity.ProfileEntity;
import com.sravan.moneymanager.Repo.ProfileRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepo profileRepo;
    private final EmailService emailService;
    private final ExpenseService expenseService;
    @Value("${moneyManger.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "1 01 22 * * * ", zone = "Asia/Kolkata")
//    @Scheduled(cron = "0 * * * * * ", zone = "Asia/Kolkata")
    public void sendDailyIncomeExpenseNotification() {
        log.info(">>>Sending daily income and expense notification");
        List<ProfileEntity> profiles = profileRepo.findAll();
        profiles.forEach(profile -> {
            String body = "<!DOCTYPE html>"
                    + "<html lang='en'>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "</head>"
                    + "<body style='margin: 0; padding: 0; background-color: #0a0a0a; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif;'>"
                    + "<table width='100%' cellpadding='0' cellspacing='0' style='background-color: #0a0a0a; padding: 40px 20px;'>"
                    + "<tr><td align='center'>"
                    + "<table width='600' cellpadding='0' cellspacing='0' style='background-color: #1a1a1a; border-radius: 12px; border: 1px solid #2a2a2a; overflow: hidden;'>"
                    + "<!-- Header -->"
                    + "<tr><td style='background-color: #2a2a2a; padding: 40px 30px; text-align: center; border-bottom: 1px solid #3a3a3a;'>"
                    + "<h1 style='margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;'>💰 Money Manager</h1>"
                    + "<p style='margin: 10px 0 0 0; color: #888888; font-size: 14px;'>Daily Finance Reminder</p>"
                    + "</td></tr>"
                    + "<!-- Content -->"
                    + "<tr><td style='padding: 40px;'>"
                    + "<h2 style='margin: 0 0 20px 0; color: #e0e0e0; font-size: 20px; font-weight: 500;'>Hi " + profile.getFullName() + "! 👋</h2>"
                    + "<p style='margin: 0 0 25px 0; color: #b0b0b0; font-size: 15px; line-height: 1.6;'>Don't forget to track your finances for today. Staying on top of your income and expenses helps you achieve your financial goals.</p>"
                    + "<div style='background-color: #242424; border-left: 3px solid #4a9eff; padding: 18px; margin: 25px 0; border-radius: 6px;'>"
                    + "<p style='margin: 0; color: #c0c0c0; font-size: 14px; line-height: 1.6;'>💡 <strong>Quick Tip:</strong> Regular tracking helps you identify spending patterns and make smarter decisions.</p>"
                    + "</div>"
                    + "<!-- CTA Button -->"
                    + "<table width='100%' cellpadding='0' cellspacing='0' style='margin: 30px 0;'>"
                    + "<tr><td align='center'>"
                    + "<a href='" +  (frontendUrl.endsWith("/") ? frontendUrl + "expense" : frontendUrl + "/expense") + "' style='display: inline-block; background-color: #4a9eff; color: #ffffff; padding: 14px 40px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 15px;'>Open Money Manager</a>"
                    + "</td></tr></table>"
                    + "</td></tr>"
                    + "<!-- Footer -->"
                    + "<tr><td style='background-color: #242424; padding: 25px 40px; text-align: center; border-top: 1px solid #3a3a3a;'>"
                    + "<p style='margin: 0; color: #888888; font-size: 13px;'>Best regards,<br/><strong style='color: #a0a0a0;'>The Money Manager Team</strong></p>"
                    + "</td></tr>"
                    + "</table>"
                    + "</td></tr></table>"
                    + "</body>"
                    + "</html>";
            emailService.sendEmail(profile.getEmail(), "💰 Daily Reminder: Log Your Finances", body);

        });
    }

    @Scheduled(cron = "1 01 23 * * * ", zone = "Asia/Kolkata")
//    @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata")
    public void sendDailyExpenseSummaryNotification() {
        log.info(">>>Sending daily expense summary notification");
        List<ProfileEntity> profiles = profileRepo.findAll();
        profiles.forEach(profile -> {
            List<ExpenseDTO> expensesForUserOnDate = expenseService.getExpensesForUserOnDate(profile.getId(),
                    LocalDate.now(ZoneId.of(
                            "Asia/Kolkata")));
            if (!expensesForUserOnDate.isEmpty()) {
                BigDecimal total = expensesForUserOnDate.stream()
                        .map(expenseDTO -> expenseDTO.getAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                StringBuilder htmlContent = new StringBuilder();
                htmlContent.append("<!DOCTYPE html>")
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
                        .append("<tr><td style='background-color: #2a2a2a; padding: 40px 30px; text-align: center; border-bottom: 1px solid #3a3a3a;'>")
                        .append("<h1 style='margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;'>📊 Daily Expense Report</h1>")
                        .append("<p style='margin: 10px 0 0 0; color: #888888; font-size: 14px;'>Your spending summary for today</p>")
                        .append("</td></tr>")
                        .append("<!-- Content -->")
                        .append("<tr><td style='padding: 40px;'>")
                        .append("<h2 style='margin: 0 0 20px 0; color: #e0e0e0; font-size: 20px; font-weight: 500;'>Hi ")
                        .append(profile.getFullName())
                        .append("! 👋</h2>")
                        .append("<p style='margin: 0 0 25px 0; color: #b0b0b0; font-size: 15px; line-height: 1.6;'>Here's a breakdown of your expenses for today:</p>")
                        .append("<!-- Expense Table -->")
                        .append("<table width='100%' cellpadding='0' cellspacing='0' style='border-radius: 8px; overflow: hidden; margin: 20px 0; border: 1px solid #2a2a2a;'>")
                        .append("<thead>")
                        .append("<tr style='background-color: #2a2a2a;'>")
                        .append("<th style='padding: 14px 12px; text-align: left; color: #e0e0e0; font-weight: 600; font-size: 13px;'>Category</th>")
                        .append("<th style='padding: 14px 12px; text-align: left; color: #e0e0e0; font-weight: 600; font-size: 13px;'>Description</th>")
                        .append("<th style='padding: 14px 12px; text-align: right; color: #e0e0e0; font-weight: 600; font-size: 13px;'>Amount</th>")
                        .append("</tr>")
                        .append("</thead>")
                        .append("<tbody>");

                boolean isAlternate = false;
                for (ExpenseDTO expense : expensesForUserOnDate) {
                    htmlContent.append("<tr style='background-color: ")
                            .append(isAlternate ? "#222222" : "#1a1a1a")
                            .append("; border-bottom: 1px solid #2a2a2a;'>")
                            .append("<td style='padding: 14px 12px; color: #c0c0c0; font-size: 14px;'>")
                            .append(expense.getCategoryName())
                            .append("</td>")
                            .append("<td style='padding: 14px 12px; color: #a0a0a0; font-size: 14px;'>")
                            .append(expense.getName())
                            .append("</td>")
                            .append("<td style='padding: 14px 12px; color: #e0e0e0; font-weight: 600; font-size: 14px; text-align: right;'>₹")
                            .append(expense.getAmount())
                            .append("</td>")
                            .append("</tr>");
                    isAlternate = !isAlternate;
                }

                htmlContent.append("<tr style='background-color: #2a2a2a; border-top: 2px solid #3a3a3a;'>")
                        .append("<td colspan='2' style='padding: 18px 12px; text-align: right; color: #e0e0e0; font-weight: 700; font-size: 15px;'>Total Spent Today</td>")
                        .append("<td style='padding: 18px 12px; text-align: right; color: #ff6b6b; font-weight: 700; font-size: 16px;'>₹")
                        .append(total)
                        .append("</td>")
                        .append("</tr>")
                        .append("</tbody></table>")
                        .append("<!-- CTA Button -->")
                        .append("<table width='100%' cellpadding='0' cellspacing='0' style='margin: 30px 0;'>")
                        .append("<tr><td align='center'>")
                        .append("<a href='")
                        .append(frontendUrl.endsWith("/") ? frontendUrl + "dashboard" : frontendUrl + "/dashboard")
                        .append("' style='display: inline-block; background-color: #4a9eff; color: #ffffff; padding: 14px 40px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 15px;'>View Full Dashboard</a>")
                        .append("</td></tr></table>")
                        .append("</td></tr>")
                        .append("<!-- Footer -->")
                        .append("<tr><td style='background-color: #242424; padding: 25px 40px; text-align: center; border-top: 1px solid #3a3a3a;'>")
                        .append("<p style='margin: 0; color: #888888; font-size: 13px;'>Best regards,<br/><strong style='color: #a0a0a0;'>The Money Manager Team</strong></p>")
                        .append("</td></tr>")
                        .append("</table>")
                        .append("</td></tr></table>")
                        .append("</body>")
                        .append("</html>");

                emailService.sendEmail(profile.getEmail(), "📊 Your Daily Expense Summary", htmlContent.toString());
            } else {
                String body = "<!DOCTYPE html>"
                        + "<html lang='en'>"
                        + "<head>"
                        + "<meta charset='UTF-8'>"
                        + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "</head>"
                        + "<body style='margin: 0; padding: 0; background-color: #0a0a0a; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif;'>"
                        + "<table width='100%' cellpadding='0' cellspacing='0' style='background-color: #0a0a0a; padding: 40px 20px;'>"
                        + "<tr><td align='center'>"
                        + "<table width='600' cellpadding='0' cellspacing='0' style='background-color: #1a1a1a; border-radius: 12px; border: 1px solid #2a2a2a; overflow: hidden;'>"
                        + "<!-- Header -->"
                        + "<tr><td style='background-color: #2a2a2a; padding: 40px 30px; text-align: center; border-bottom: 1px solid #3a3a3a;'>"
                        + "<h1 style='margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;'>✨ Great Day!</h1>"
                        + "<p style='margin: 10px 0 0 0; color: #888888; font-size: 14px;'>No expenses recorded today</p>"
                        + "</td></tr>"
                        + "<!-- Content -->"
                        + "<tr><td style='padding: 40px; text-align: center;'>"
                        + "<div style='margin-bottom: 25px;'>"
                        + "<div style='font-size: 56px; margin-bottom: 20px;'>🎉</div>"
                        + "<h2 style='margin: 0 0 20px 0; color: #e0e0e0; font-size: 20px; font-weight: 500;'>Hi " + profile.getFullName() + "!</h2>"
                        + "<p style='margin: 0 0 25px 0; color: #b0b0b0; font-size: 15px; line-height: 1.6;'>We noticed you haven't logged any expenses today. That's either great news or you might have forgotten to track some transactions!</p>"
                        + "</div>"
                        + "<div style='background-color: #242424; border-radius: 8px; padding: 20px; margin: 25px 0;'>"
                        + "<p style='margin: 0; color: #c0c0c0; font-size: 14px; line-height: 1.6;'>💡 <strong>Remember:</strong> Even small purchases add up. Keep your financial records up to date for better insights!</p>"
                        + "</div>"
                        + "<!-- CTA Button -->"
                        + "<table width='100%' cellpadding='0' cellspacing='0' style='margin: 30px 0;'>"
                        + "<tr><td align='center'>"
                        + "<a href='" + (frontendUrl.endsWith("/") ? frontendUrl + "dashboard" : frontendUrl + "/dashboard") + "' style='display: inline-block; background-color: #4a9eff; color: #ffffff; padding: 14px 40px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 15px;'>Check Your Dashboard</a>"
                        + "</td></tr></table>"
                        + "</td></tr>"
                        + "<!-- Footer -->"
                        + "<tr><td style='background-color: #242424; padding: 25px 40px; text-align: center; border-top: 1px solid #3a3a3a;'>"
                        + "<p style='margin: 0; color: #888888; font-size: 13px;'>Best regards,<br/><strong style='color: #a0a0a0;'>The Money Manager Team</strong></p>"
                        + "</td></tr>"
                        + "</table>"
                        + "</td></tr></table>"
                        + "</body>"
                        + "</html>";

                emailService.sendEmail(profile.getEmail(), "✨ Daily Expense Summary - No Expenses Today!", body);
            }
        });
    }
}
