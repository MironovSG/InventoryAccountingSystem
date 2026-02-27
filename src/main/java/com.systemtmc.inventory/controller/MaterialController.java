package com.systemtmc.inventory.controller;

import com.systemtmc.inventory.dto.MaterialDTO;
import com.systemtmc.inventory.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления ТМЦ
 */
@RestController
@RequestMapping("/materials")
@RequiredArgsConstructor
@Tag(name = "Materials", description = "API для управления ТМЦ")
public class MaterialController {
    
    private final MaterialService materialService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL', 'ENGINEER')")
    @Operation(summary = "Получить все материалы")
    public ResponseEntity<List<MaterialDTO>> getAllMaterials() {
        return ResponseEntity.ok(materialService.getAllMaterials());
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL', 'ENGINEER')")
    @Operation(summary = "Получить активные материалы")
    public ResponseEntity<List<MaterialDTO>> getActiveMaterials() {
        return ResponseEntity.ok(materialService.getActiveMaterials());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL', 'ENGINEER')")
    @Operation(summary = "Получить материал по ID")
    public ResponseEntity<MaterialDTO> getMaterialById(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.getMaterialById(id));
    }
    
    @GetMapping("/article/{article}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL', 'ENGINEER')")
    @Operation(summary = "Получить материал по артикулу")
    public ResponseEntity<MaterialDTO> getMaterialByArticle(@PathVariable String article) {
        return ResponseEntity.ok(materialService.getMaterialByArticle(article));
    }
    
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL', 'ENGINEER')")
    @Operation(summary = "Получить материалы по категории")
    public ResponseEntity<List<MaterialDTO>> getMaterialsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(materialService.getMaterialsByCategory(categoryId));
    }
    
    @GetMapping("/in-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL', 'ENGINEER')")
    @Operation(summary = "Получить материалы в наличии")
    public ResponseEntity<List<MaterialDTO>> getInStockMaterials() {
        return ResponseEntity.ok(materialService.getInStockMaterials());
    }
    
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить материалы с низким запасом")
    public ResponseEntity<List<MaterialDTO>> getLowStockMaterials() {
        return ResponseEntity.ok(materialService.getLowStockMaterials());
    }
    
    @GetMapping("/critical-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить материалы с критическим запасом")
    public ResponseEntity<List<MaterialDTO>> getCriticalStockMaterials() {
        return ResponseEntity.ok(materialService.getCriticalStockMaterials());
    }
    
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить материалы не в наличии")
    public ResponseEntity<List<MaterialDTO>> getOutOfStockMaterials() {
        return ResponseEntity.ok(materialService.getOutOfStockMaterials());
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL', 'ENGINEER')")
    @Operation(summary = "Поиск материалов")
    public ResponseEntity<List<MaterialDTO>> searchMaterials(@RequestParam String query) {
        return ResponseEntity.ok(materialService.searchMaterials(query));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MOL')")
    @Operation(summary = "Создать материал")
    public ResponseEntity<MaterialDTO> createMaterial(@Valid @RequestBody MaterialDTO materialDTO) {
        return ResponseEntity.ok(materialService.createMaterial(materialDTO));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOL')")
    @Operation(summary = "Обновить материал")
    public ResponseEntity<MaterialDTO> updateMaterial(@PathVariable Long id, @Valid @RequestBody MaterialDTO materialDTO) {
        return ResponseEntity.ok(materialService.updateMaterial(id, materialDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOL')")
    @Operation(summary = "Удалить материал")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok().build();
    }
}
