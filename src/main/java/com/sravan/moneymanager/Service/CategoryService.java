package com.sravan.moneymanager.Service;

import com.sravan.moneymanager.DTO.CategoryDTO;
import com.sravan.moneymanager.Entity.CategoryEntity;
import com.sravan.moneymanager.Entity.ProfileEntity;
import com.sravan.moneymanager.Repo.CategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepo categoryRepo;

    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        if (categoryRepo.existsByNameAndProfileId(categoryDTO.getName(), profileEntity.getId())) {

            throw new RuntimeException("Category with name " + categoryDTO.getName() + " already exists");

        }
        CategoryEntity categoryEntity = toEntity(categoryDTO, profileEntity);
        CategoryEntity savedCategory = categoryRepo.save(categoryEntity);
        return toDTO(savedCategory);
    }
//    Get Categories for Current User
    public List<CategoryDTO> getCategoriesForCurrentUser(){
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepo.findByProfileId(profileEntity.getId());
        List<CategoryDTO> categoryDTOList = categories.stream()
                                                        .map(categoryEntity -> toDTO(categoryEntity)).toList();
        return categoryDTOList;

    }
// Get Categories by Type for Current User
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type){
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepo.findByTypeAndProfileId(type, profileEntity.getId());
        List<CategoryDTO> categoryDTOList = categories.stream()
                .map(categoryEntity -> toDTO(categoryEntity)).toList();
        return categoryDTOList;
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO){
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        CategoryEntity retrievedCategory = categoryRepo.findByIdAndProfileId(id, profileEntity.getId())
                                                       .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        retrievedCategory.setName(categoryDTO.getName());
        retrievedCategory.setIcon(categoryDTO.getIcon());
        retrievedCategory.setType(categoryDTO.getType());
        CategoryEntity updatedCategory = categoryRepo.save(retrievedCategory);
        return toDTO(updatedCategory);

    }

    private CategoryEntity toEntity(CategoryDTO CategoryDTO, ProfileEntity profileEntity) {

        return CategoryEntity.builder()
                             .name(CategoryDTO.getName())
                             .profile(profileEntity)
                             .icon(CategoryDTO.getIcon())
                             .type(CategoryDTO.getType())
                             .build();

    }

    public CategoryDTO toDTO(CategoryEntity categoryEntity) {
        return CategoryDTO.builder()
                          .id(categoryEntity.getId())
                          .profileId(categoryEntity.getProfile() != null ? categoryEntity.getProfile()
                                                                                         .getId() : null)
                          .name(categoryEntity.getName())
                          .icon(categoryEntity.getIcon())
                          .type(categoryEntity.getType())
                          .createdAt(categoryEntity.getCreatedAt())
                          .updatedAt(categoryEntity.getUpdatedAt())
                          .build();
    }
}
