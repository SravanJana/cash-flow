package com.sravan.moneymanager.Repo;

import com.sravan.moneymanager.Entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByProfileId(Long profileId);

    Optional<CategoryEntity> findByIdAndProfileId(Long id, Long profileId);

    List<CategoryEntity> findByTypeAndProfileId(String type, Long profile_id);

    boolean existsByNameAndProfileId(String name, Long profileId);
}
