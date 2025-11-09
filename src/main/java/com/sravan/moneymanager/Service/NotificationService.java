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


    @Scheduled(cron = "1 01 22 * * * ",zone = "Asia/Kolkata")
    public void sendDailyIncomeExpenseNotification() {
        log.info(">>>Sending daily income and expense notification");
        List<ProfileEntity> profiles = profileRepo.findAll();
        profiles.forEach(profile -> {
            String body = "Hi " + profile.getFullName() + ",<br/><br/>"
                    + "This is a friendly reminder to add your income and expenses for today in money manager.<br/><br/>"
                    + "<a href='" + frontendUrl + "' style='display:inline-block; background-color:#007bff; color:white; padding:10px; border-radius:5px; text-decoration:none;'>Go to Money Manager</a><br/><br/>"
                    + "Thanks,<br/>Money Manager Team";
            emailService.sendEmail(profile.getEmail(), "Daily reminder : Add you income and expenses", body);


        });
    }

//    @Scheduled(cron = "1 01 23 * * * ",zone = "Asia/Kolkata" )
    @Scheduled(cron = "0 * * * * *",zone = "Asia/Kolkata" )
    public void sendDailyExpenseSummaryNotification(){
        log.info(">>>Sending daily expense summary notification");
        List<ProfileEntity> profiles = profileRepo.findAll();
        profiles.forEach(profile -> {
            List<ExpenseDTO> expensesForUserOnDate = expenseService.getExpensesForUserOnDate(profile.getId(),
                                                                                             LocalDate.now(ZoneId.of(
                                                                                                     "Asia/Kolkata")));
            if(!expensesForUserOnDate.isEmpty()){
                BigDecimal total = expensesForUserOnDate.stream()
                                                        .map(expenseDTO -> expenseDTO.getAmount())
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                StringBuilder htmlContent = new StringBuilder();
                htmlContent.append("Hi ")
                           .append(profile.getFullName())
                           .append(",<br/><br/>")
                           .append("Here's your expense summary for today:<br/><br/>")
                           .append("<table style='border-collapse: collapse; width: 100%; max-width: 600px;'>")
                           .append("<tr style='background-color: #007bff; color: white;'>")
                           .append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Category</th>")
                           .append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Description</th>")
                           .append("<th style='padding: 12px; text-align: right; border: 1px solid #ddd;'>Amount</th>")
                           .append("</tr>");

                expensesForUserOnDate.forEach(expense -> {
                    htmlContent.append("<tr style='background-color: #f8f9fa;'>")
                               .append("<td style='padding: 12px; text-align: left; border: 1px solid #ddd;'>")
                               .append(expense.getCategoryName())
                               .append("</td>")
                               .append("<td style='padding: 12px; text-align: left; border: 1px solid #ddd;'>")
                               .append(expense.getName())
                               .append("</td>")
                               .append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'>₹")
                               .append(expense.getAmount())
                               .append("</td>")
                               .append("</tr>");
                });

                htmlContent.append("<tr style='background-color: #e9ecef;'>")
                           .append("<td colspan='2' style='padding: 12px; text-align: right; border: 1px solid #ddd;'><strong>Total</strong></td>")
                           .append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'><strong>₹")
                           .append(total)
                           .append("</strong></td>")
                           .append("</tr>")
                           .append("</table><br/><br/>")
                           .append("<a href='")
                           .append(frontendUrl)
                           .append("' style='display:inline-block; background-color:#007bff; color:white; padding:10px; border-radius:5px; text-decoration:none;'>View Details</a><br/><br/>")
                           .append("Thanks,<br/>Money Manager Team");

                emailService.sendEmail(profile.getEmail(), "Daily Expense Summary", htmlContent.toString());
            }
        });
    }
}
