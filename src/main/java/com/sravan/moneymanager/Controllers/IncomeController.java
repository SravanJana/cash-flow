package com.sravan.moneymanager.Controllers;

import com.sravan.moneymanager.DTO.IncomeDTO;
import com.sravan.moneymanager.Service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDTO> addExpenses(@RequestBody IncomeDTO  incomeDTO) {
        IncomeDTO saved = incomeService.addIncome(incomeDTO);
        return ResponseEntity.status(201)
                             .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getAllIncomes(){
        List<IncomeDTO> allIncomes = incomeService.getCurrentMonthIncomesForCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(allIncomes);

    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteIncome(@PathVariable Long id){
        incomeService.deleteIncomeById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Income Deleted Successfully");
    }
}
