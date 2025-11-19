package com.sravan.moneymanager.Service;

import com.sravan.moneymanager.DTO.ExpenseDTO;
import com.sravan.moneymanager.Entity.CategoryEntity;
import com.sravan.moneymanager.Entity.ExpenseEntity;
import com.sravan.moneymanager.Entity.ProfileEntity;
import com.sravan.moneymanager.Repo.CategoryRepo;
import com.sravan.moneymanager.Repo.ExpenseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepo categoryRepo;
    private final ExpenseRepo expenseRepo;
    private final ProfileService profileService;

    public ExpenseDTO addExpense(ExpenseDTO expenseDTO) {

        ProfileEntity profileEntity = profileService.getCurrentProfile();
        CategoryEntity categoryEntity = categoryRepo.findById(expenseDTO.getCategoryId())
                                                    .orElseThrow(() -> new RuntimeException(
                                                            "Category not found with id: " + expenseDTO.getCategoryId()));
        ExpenseEntity entity = toEntity(expenseDTO, profileEntity, categoryEntity);
        ExpenseEntity savedEntity = expenseRepo.save(entity);
        return toDTO(savedEntity);

    }

    public List<ExpenseDTO> getAllExpenses() {
        List<ExpenseEntity> expenseEntities = expenseRepo.findAll();
        List<ExpenseDTO> expenseDTOs = expenseEntities.stream()
                                                      .map(this::toDTO)
                                                      .collect(Collectors.toList());
        return expenseDTOs;
    }

    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        LocalDate startDate = LocalDate.now()
                                       .withDayOfMonth(1);
        LocalDate endDate = LocalDate.now()
                                     .withDayOfMonth(LocalDate.now()
                                                              .lengthOfMonth());
        List<ExpenseEntity> list = expenseRepo.findByProfileIdAndDateBetween(currentProfile.getId(), startDate,
                                                                             endDate);
        return list.stream()
                   .map(entity -> toDTO(entity))
                   .toList();
    }

    public void deleteExpenseById(Long expenseID){
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        ExpenseEntity entity = expenseRepo.findById(expenseID)
                                                 .orElseThrow(() -> new RuntimeException(
                                                         "Expense not found with id: " + expenseID));
        if(!entity.getProfile().getId().equals(currentProfile.getId())){
            throw new RuntimeException("unauthorized to delete expense");


        }
            expenseRepo.deleteById(expenseID);
    }

    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser(){
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        List<ExpenseEntity> list = expenseRepo.findTop5ByProfileIdAndDateBetweenOrderByDateDesc(
                currentProfile.getId(), startDate, endDate);
        return list.stream()
                   .map(entity -> toDTO(entity))
                   .toList();
    }

    public BigDecimal getTotalExpenditureForCurrentUser(){
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        BigDecimal totalExpenseByProfileId = expenseRepo.findTotalExpenseByProfileId(currentProfile.getId());
        return totalExpenseByProfileId != null ? totalExpenseByProfileId : BigDecimal.ZERO;
    }

    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword  , Sort sort){
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepo.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(currentProfile.getId(), startDate, endDate, keyword, sort);
        return list.stream()
                   .map(entity -> toDTO(entity))
                   .toList();

    }

    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId,LocalDate date){
        List<ExpenseEntity> byProfileIdAndDate = expenseRepo.findByProfileIdAndDate(profileId, date);
        return byProfileIdAndDate.stream()
                   .map(entity -> toDTO(entity))
                   .toList();
    }


    public ExpenseEntity toEntity(ExpenseDTO expenseDTO, ProfileEntity profileEntity, CategoryEntity categoryEntity) {
        return ExpenseEntity.builder()
                            .name(expenseDTO.getName())
                            .icon(expenseDTO.getIcon())
                            .amount(expenseDTO.getAmount())
                            .date(expenseDTO.getDate())
                            .category(categoryEntity)
                            .profile(profileEntity)
                            .build();

    }

    public ExpenseDTO toDTO(ExpenseEntity expenseEntity) {
        return ExpenseDTO.builder()
                         .id(expenseEntity.getId())
                         .name(expenseEntity.getName())
                         .icon(expenseEntity.getIcon())
                         .categoryId(expenseEntity.getCategory() != null ? expenseEntity.getCategory()
                                                                                        .getId() : null)
                         .categoryName(expenseEntity.getCategory() != null ? expenseEntity.getCategory()
                                                                                          .getName() : null)
                         .amount(expenseEntity.getAmount())
                         .date(expenseEntity.getDate())
                         .createdAt(expenseEntity.getCreatedAt())
                         .updatedAt(expenseEntity.getUpdatedAt())
                         .build();
    }
}
