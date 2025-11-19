package com.sravan.moneymanager.Repo;

import com.sravan.moneymanager.Entity.ExpenseEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepo extends JpaRepository<ExpenseEntity, Long> {

    // select * from expenses where profile_id = ? order by date desc
    List<ExpenseEntity> findByProfileIdOrderByDateDesc(Long profileId);

    // select * from expenses where profile_id = ? order by date desc limit 5
    List<ExpenseEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    List<ExpenseEntity> findTop5ByProfileIdAndDateBetweenOrderByDateDesc(Long profileId, LocalDate startDate, LocalDate endDate);

    @Query("select sum(e.amount) from ExpenseEntity e where e.profile.id = :profileId")
    BigDecimal findTotalExpenseByProfileId(@Param("profileId") Long profileId);

    @Query("select sum(e.amount) from ExpenseEntity e where e.profile.id = :profileId and e.date between :startDate and :endDate")
    BigDecimal findTotalExpenseByProfileIdAndDateBetween(@Param("profileId") Long profileId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    //  select * from expenses where profile_id = ? and date between ? and ? and name like %?% order by date desc
    List<ExpenseEntity> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(Long profileId,
                                                                                 LocalDate dateAfter,
                                                                                 LocalDate dateBefore,
                                                                                 String keyword, Sort sort);

    // select * from expenses where profile_id = ? and date between ? and ?
    List<ExpenseEntity> findByProfileIdAndDateBetween(Long profile_id, LocalDate date, LocalDate date2);

    // select * from expenses where profile_id = ? and date = ?
    @EntityGraph(attributePaths = {"category"})
    List<ExpenseEntity> findByProfileIdAndDate(Long profile_id, LocalDate date);

}
