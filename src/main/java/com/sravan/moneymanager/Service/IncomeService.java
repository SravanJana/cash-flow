package com.sravan.moneymanager.Service;

import com.sravan.moneymanager.DTO.ExpenseDTO;
import com.sravan.moneymanager.DTO.IncomeDTO;
import com.sravan.moneymanager.Entity.CategoryEntity;
import com.sravan.moneymanager.Entity.ExpenseEntity;
import com.sravan.moneymanager.Entity.IncomeEntity;
import com.sravan.moneymanager.Entity.ProfileEntity;
import com.sravan.moneymanager.Repo.CategoryRepo;
import com.sravan.moneymanager.Repo.IncomeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final CategoryRepo categoryRepo;
    private final IncomeRepo incomeRepo;
    private final ProfileService profileService;


    public IncomeDTO addIncome(IncomeDTO incomeDTO){
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        CategoryEntity categoryEntity = categoryRepo.findById(incomeDTO.getCategoryId())
                                                    .orElseThrow(() -> new RuntimeException(
                                                            "Category not found with id: " + incomeDTO.getCategoryId()));
        IncomeEntity entity = toEntity(incomeDTO, profileEntity, categoryEntity);
        IncomeEntity savedEntity = incomeRepo.save(entity);
        return toDTO(savedEntity);

    }

    public List<IncomeDTO> getAllIncomes(){
        List<IncomeEntity> incomeEntities = incomeRepo.findAll();
        List<IncomeDTO> incomeDTOList = incomeEntities.stream()
                                                .map(income -> toDTO(income))
                                                .collect(Collectors.toList());
        return incomeDTOList;
    }

    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser() {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        LocalDate startDate = LocalDate.now()
                                       .withDayOfMonth(1);
        LocalDate endDate = LocalDate.now()
                                     .withDayOfMonth(LocalDate.now()
                                                              .lengthOfMonth());
        List<IncomeEntity> list = incomeRepo.findByProfileIdAndDateBetween(currentProfile.getId(), startDate,
                                                                             endDate);
        return list.stream().map(entity -> toDTO(entity)).toList();
    }



    public void deleteIncomeById(Long expenseID){
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        IncomeEntity entity = incomeRepo.findById(expenseID)
                                          .orElseThrow(() -> new RuntimeException(
                                                  "Expense not found with id: " + expenseID));
        if(!entity.getProfile().getId().equals(currentProfile.getId())){
            throw new RuntimeException("unauthorized to delete expense");


        }
        incomeRepo.deleteById(expenseID);
    }

    public List<IncomeDTO> getLatest5IncomesForCurrentUser(){
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        List<IncomeEntity> list = incomeRepo.findTop5ByProfileIdAndDateBetweenOrderByDateDesc(currentProfile.getId(), startDate, endDate);
        return list.stream()
                   .map(entity -> toDTO(entity))
                   .toList();
    }


    public BigDecimal getTotalIncomeForCurrentMonthForCurrentUser(){
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        BigDecimal totalIncomeByProfileId = incomeRepo.findTotalIncomeByProfileIdAndDateBetween(currentProfile.getId(), startDate, endDate);
        return totalIncomeByProfileId != null ? totalIncomeByProfileId : BigDecimal.ZERO;
    }


    public List<IncomeDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword  , Sort sort){
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepo.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(currentProfile.getId(), startDate, endDate, keyword, sort);
        return list.stream()
                   .map(entity -> toDTO(entity))
                   .toList();

    }


    public IncomeEntity toEntity(IncomeDTO incomeDTO, ProfileEntity profileEntity, CategoryEntity categoryEntity) {
        return IncomeEntity.builder()
                            .name(incomeDTO.getName())
                            .icon(incomeDTO.getIcon())
                            .amount(incomeDTO.getAmount())
                            .date(incomeDTO.getDate())
                            .category(categoryEntity)
                            .profile(profileEntity)
                            .build();

    }

    public IncomeDTO toDTO(IncomeEntity incomeEntity) {
        return IncomeDTO.builder()
                         .id(incomeEntity.getId())
                         .name(incomeEntity.getName())
                         .icon(incomeEntity.getIcon())
                         .categoryId(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getId() : null)
                         .categoryName(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getName() : null)
                         .amount(incomeEntity.getAmount())
                         .date(incomeEntity.getDate())
                         .createdAt(incomeEntity.getCreatedAt())
                         .updatedAt(incomeEntity.getUpdatedAt())
                         .build();
    }


}
