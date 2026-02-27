package com.systemtmc.inventory.controller;

import com.systemtmc.inventory.dto.MaterialCategoryDTO;
import com.systemtmc.inventory.service.MaterialCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления категориями материалов
 */
@RestController
@RequestMapping("/material-categories")
@RequiredArgsConstructor
@Tag(name = "Material Categories", description = "API для управления категориями ТМЦ")
public class MaterialCategoryController {
    
    private final MaterialCategoryService categoryService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить все категории")
    public ResponseEntity<List<MaterialCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL', 'ENGINEER')")
    @Operation(summary = "Получить активные категории")
    public ResponseEntity<List<MaterialCategoryDTO>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить категорию по ID")
    public ResponseEntity<MaterialCategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать категорию")
    public ResponseEntity<MaterialCategoryDTO> createCategory(@Valid @RequestBody MaterialCategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDTO));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить категорию")
    public ResponseEntity<MaterialCategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody MaterialCategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить категорию")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
