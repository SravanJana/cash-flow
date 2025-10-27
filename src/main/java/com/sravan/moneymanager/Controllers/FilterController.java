package com.sravan.moneymanager.Controllers;

import com.sravan.moneymanager.DTO.FilterDTO;
import com.sravan.moneymanager.Service.ExpenseService;
import com.sravan.moneymanager.Service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filterDTO){
        LocalDate startDate = filterDTO.getStartDate() != null ? filterDTO.getStartDate() : LocalDate.MIN;

        LocalDate endDate = filterDTO.getEndDate() != null ? filterDTO.getEndDate() : LocalDate.now();
        String keyword = filterDTO.getKeyword() != null ? filterDTO.getKeyword() : "";
        String sortField = filterDTO.getSortField() != null ? filterDTO.getSortField() : "date";
        Sort.Direction direction = "desc".equalsIgnoreCase(filterDTO.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        if("income".equalsIgnoreCase(filterDTO.getType())){
            return ResponseEntity.ok(incomeService.filterExpenses(startDate, endDate, keyword, sort));
        }else if("expense".equalsIgnoreCase(filterDTO.getType())){
            return ResponseEntity.ok(expenseService.filterExpenses(startDate, endDate, keyword, sort));
        } else {
            return ResponseEntity.badRequest().body("Invalid Filter Type. Must be either 'income' or 'expense'");
        }
    }
}
