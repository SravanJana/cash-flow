package com.sravan.moneymanager.Service;

import com.sravan.moneymanager.DTO.ExpenseDTO;
import com.sravan.moneymanager.Entity.CategoryEntity;
import com.sravan.moneymanager.Entity.ExpenseEntity;
import com.sravan.moneymanager.Entity.ProfileEntity;
import com.sravan.moneymanager.Repo.CategoryRepo;
import com.sravan.moneymanager.Repo.ExpenseRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepo expenseRepo;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ExpenseService expenseService;

    private ProfileEntity testProfile;
    private CategoryEntity testCategory;

    @BeforeEach
    void setUp() {
        testProfile = ProfileEntity.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .build();

        testCategory = CategoryEntity.builder()
                .id(1L)
                .name("Food")
                .icon("🍔")
                .build();
    }

    @Test
    void testGetLatest5ExpensesForCurrentUser_ReturnsOnlyCurrentMonthExpenses() {
        // Arrange
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        // Create test expenses for current month
        ExpenseEntity expense1 = createExpenseEntity(1L, "Groceries", new BigDecimal("50.00"), now.minusDays(1));
        ExpenseEntity expense2 = createExpenseEntity(2L, "Restaurant", new BigDecimal("30.00"), now.minusDays(2));
        ExpenseEntity expense3 = createExpenseEntity(3L, "Coffee", new BigDecimal("5.00"), now.minusDays(3));
        ExpenseEntity expense4 = createExpenseEntity(4L, "Lunch", new BigDecimal("15.00"), now.minusDays(5));
        ExpenseEntity expense5 = createExpenseEntity(5L, "Snacks", new BigDecimal("10.00"), now.minusDays(7));

        List<ExpenseEntity> mockExpenses = Arrays.asList(expense1, expense2, expense3, expense4, expense5);

        when(profileService.getCurrentProfile()).thenReturn(testProfile);
        when(expenseRepo.findTop5ByProfileIdAndDateBetweenOrderByDateDesc(
                eq(testProfile.getId()), eq(startOfMonth), eq(endOfMonth)))
                .thenReturn(mockExpenses);

        // Act
        List<ExpenseDTO> result = expenseService.getLatest5ExpensesForCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("Groceries", result.get(0).getName());
        assertEquals("Restaurant", result.get(1).getName());
        assertEquals("Coffee", result.get(2).getName());
        assertEquals("Lunch", result.get(3).getName());
        assertEquals("Snacks", result.get(4).getName());

        // Verify that the repository was called with correct date range
        verify(expenseRepo).findTop5ByProfileIdAndDateBetweenOrderByDateDesc(
                eq(testProfile.getId()), eq(startOfMonth), eq(endOfMonth));
        verify(profileService).getCurrentProfile();
    }

    @Test
    void testGetLatest5ExpensesForCurrentUser_ReturnsLessThan5IfNotEnoughExpenses() {
        // Arrange
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        // Create only 3 test expenses for current month
        ExpenseEntity expense1 = createExpenseEntity(1L, "Groceries", new BigDecimal("50.00"), now.minusDays(1));
        ExpenseEntity expense2 = createExpenseEntity(2L, "Restaurant", new BigDecimal("30.00"), now.minusDays(2));
        ExpenseEntity expense3 = createExpenseEntity(3L, "Coffee", new BigDecimal("5.00"), now.minusDays(3));

        List<ExpenseEntity> mockExpenses = Arrays.asList(expense1, expense2, expense3);

        when(profileService.getCurrentProfile()).thenReturn(testProfile);
        when(expenseRepo.findTop5ByProfileIdAndDateBetweenOrderByDateDesc(
                eq(testProfile.getId()), eq(startOfMonth), eq(endOfMonth)))
                .thenReturn(mockExpenses);

        // Act
        List<ExpenseDTO> result = expenseService.getLatest5ExpensesForCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Groceries", result.get(0).getName());
        assertEquals("Restaurant", result.get(1).getName());
        assertEquals("Coffee", result.get(2).getName());
    }

    @Test
    void testGetLatest5ExpensesForCurrentUser_ReturnsEmptyListWhenNoExpenses() {
        // Arrange
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        when(profileService.getCurrentProfile()).thenReturn(testProfile);
        when(expenseRepo.findTop5ByProfileIdAndDateBetweenOrderByDateDesc(
                eq(testProfile.getId()), eq(startOfMonth), eq(endOfMonth)))
                .thenReturn(Arrays.asList());

        // Act
        List<ExpenseDTO> result = expenseService.getLatest5ExpensesForCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    private ExpenseEntity createExpenseEntity(Long id, String name, BigDecimal amount, LocalDate date) {
        return ExpenseEntity.builder()
                .id(id)
                .name(name)
                .icon("💰")
                .amount(amount)
                .date(date)
                .category(testCategory)
                .profile(testProfile)
                .build();
    }
}
