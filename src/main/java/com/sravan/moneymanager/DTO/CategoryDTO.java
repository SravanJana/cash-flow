package com.sravan.moneymanager.DTO;

import com.sravan.moneymanager.Entity.ProfileEntity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;
    private Long profileId;
    private String name;
    private String icon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String type;

}
