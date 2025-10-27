package com.sravan.moneymanager.Service;

import com.sravan.moneymanager.DTO.ExpenseDTO;
import com.sravan.moneymanager.DTO.IncomeDTO;
import com.sravan.moneymanager.DTO.RecentTransactionDTO;
import com.sravan.moneymanager.Entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProfileService profileService;
    private final CategoryService categoryService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public Map<String, Object> getDashboardData() {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        HashMap<String, Object> dashboardData = new LinkedHashMap<>();
        List<IncomeDTO> latest5IncomesForCurrentUser = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> latest5ExpensesForCurrentUser = expenseService.getLatest5ExpensesForCurrentUser();
        Stream<RecentTransactionDTO> expense = latest5ExpensesForCurrentUser.stream()
                                                                            .map(expenseDTO -> RecentTransactionDTO.builder()
                                                                                                                   .id(expenseDTO.getId())
                                                                                                                   .name(expenseDTO.getName())
                                                                                                                   .icon(expenseDTO.getIcon())
                                                                                                                   .amount(expenseDTO.getAmount())
                                                                                                                   .createdAt(
                                                                                                                           expenseDTO.getCreatedAt())
                                                                                                                   .updatedAt(
                                                                                                                           expenseDTO.getUpdatedAt())
                                                                                                                   .type("Expense")
                                                                                                                   .date(expenseDTO.getDate())
                                                                                                                   .build());

        Stream<RecentTransactionDTO> income = latest5IncomesForCurrentUser.stream()
                                                                          .map(incomeDTO -> RecentTransactionDTO.builder()
                                                                                                                .id(incomeDTO.getId())
                                                                                                                .name(incomeDTO.getName())
                                                                                                                .icon(incomeDTO.getIcon())
                                                                                                                .amount(incomeDTO.getAmount())
                                                                                                                .createdAt(
                                                                                                                        incomeDTO.getCreatedAt())
                                                                                                                .updatedAt(
                                                                                                                        incomeDTO.getUpdatedAt())
                                                                                                                .type("Income")
                                                                                                                .date(incomeDTO.getDate())
                                                                                                                .build());

        List<RecentTransactionDTO> recentTransactionDTOList = concat(income, expense)
                                                   .sorted((a, b) -> {
                                                       int cmp = b.getDate()
                                                                  .compareTo(a.getDate());
                                                       if (cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
                                                           return b.getCreatedAt()
                                                                   .compareTo(a.getCreatedAt());
                                                       }
                                                       return cmp;
                                                   })
                                                   .collect(Collectors.toList());


        dashboardData.put("totalBalance",incomeService.getTotalExpenditureForCurrentUser().subtract(expenseService.getTotalExpenditureForCurrentUser()));
        dashboardData.put("totalIncome",incomeService.getTotalExpenditureForCurrentUser());
        dashboardData.put("totalExpense",expenseService.getTotalExpenditureForCurrentUser());
        dashboardData.put("recent5Incomes",latest5IncomesForCurrentUser);
        dashboardData.put("recent5Expenses",latest5ExpensesForCurrentUser);
        dashboardData.put("recentTransactions",recentTransactionDTOList);
        return dashboardData;


    }
}
