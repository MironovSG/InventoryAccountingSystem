package com.systemtmc.inventory.service;

import com.systemtmc.inventory.dto.MaterialCategoryDTO;
import com.systemtmc.inventory.model.entity.MaterialCategory;
import com.systemtmc.inventory.repository.MaterialCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с категориями материалов
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialCategoryService {
    
    private final MaterialCategoryRepository categoryRepository;
    private final AuditService auditService;
    
    @Transactional(readOnly = true)
    public List<MaterialCategoryDTO> getAllCategories() {
        return categoryRepository.findByDeletedFalse().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MaterialCategoryDTO> getActiveCategories() {
        return categoryRepository.findAllActiveCategories().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public MaterialCategoryDTO getCategoryById(Long id) {
        MaterialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена с ID: " + id));
        return convertToDTO(category);
    }
    
    @Transactional
    public MaterialCategoryDTO createCategory(MaterialCategoryDTO categoryDTO) {
        if (categoryRepository.existsByCode(categoryDTO.getCode())) {
            throw new RuntimeException("Категория с таким кодом уже существует");
        }
        
        MaterialCategory category = new MaterialCategory();
        category.setCode(categoryDTO.getCode());
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setActive(categoryDTO.getActive() != null ? categoryDTO.getActive() : true);
        
        if (categoryDTO.getParentId() != null) {
            MaterialCategory parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Родительская категория не найдена"));
            category.setParent(parent);
        }
        
        MaterialCategory savedCategory = categoryRepository.save(category);
        auditService.logAction("CREATE_CATEGORY", "MaterialCategory", savedCategory.getId(), 
                "Создана категория: " + savedCategory.getName());
        
        log.info("Создана категория: {}", savedCategory.getName());
        return convertToDTO(savedCategory);
    }
    
    @Transactional
    public MaterialCategoryDTO updateCategory(Long id, MaterialCategoryDTO categoryDTO) {
        MaterialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        
        if (!category.getCode().equals(categoryDTO.getCode()) && 
            categoryRepository.existsByCode(categoryDTO.getCode())) {
            throw new RuntimeException("Категория с таким кодом уже существует");
        }
        
        category.setCode(categoryDTO.getCode());
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setActive(categoryDTO.getActive());
        
        MaterialCategory updatedCategory = categoryRepository.save(category);
        auditService.logAction("UPDATE_CATEGORY", "MaterialCategory", updatedCategory.getId(), 
                "Обновлена категория: " + updatedCategory.getName());
        
        log.info("Обновлена категория: {}", updatedCategory.getName());
        return convertToDTO(updatedCategory);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        MaterialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        
        category.setDeleted(true);
        category.setActive(false);
        categoryRepository.save(category);
        
        auditService.logAction("DELETE_CATEGORY", "MaterialCategory", id, 
                "Удалена категория: " + category.getName());
        
        log.info("Удалена категория: {}", category.getName());
    }
    
    private MaterialCategoryDTO convertToDTO(MaterialCategory category) {
        MaterialCategoryDTO dto = MaterialCategoryDTO.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .build();
        
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }
        
        return dto;
    }
}
