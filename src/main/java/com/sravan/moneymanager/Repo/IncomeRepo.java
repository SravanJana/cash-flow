package com.sravan.moneymanager.Repo;

import com.sravan.moneymanager.Entity.IncomeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public interface IncomeRepo extends JpaRepository<IncomeEntity, Long> {
    //    select * from incomes where profile_id = ? order by date desc
    List<IncomeEntity> findByProfileIdOrderByDateDesc(Long profileId);

    // select * from incomes where profile_id = ? order by date desc limit 5
    List<IncomeEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    List<IncomeEntity> findTop5ByProfileIdAndDateBetweenOrderByDateDesc(Long profileId, LocalDate startDate, LocalDate endDate);

    @Query("select sum(i.amount) from IncomeEntity i where i.profile.id = :profileId")
    BigDecimal findTotalIncomeByProfileId(@Param("profileId") Long profileId);

    @Query("select sum(i.amount) from IncomeEntity i where i.profile.id = :profileId and i.date between :startDate and :endDate")
    BigDecimal findTotalIncomeByProfileIdAndDateBetween(@Param("profileId") Long profileId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    //  select * from incomes where profile_id = ? and date between ? and ? and name like %?% order by date desc
    List<IncomeEntity> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(Long profileId, LocalDate dateAfter,
                                                                                LocalDate dateBefore, String keyword,
                                                                                Sort sort);

    // select * from incomes where profile_id = ? and date between ? and ?
    List<IncomeEntity> findByProfileIdAndDateBetween(Long profileId, LocalDate dateAfter, LocalDate dateBefore);
}
