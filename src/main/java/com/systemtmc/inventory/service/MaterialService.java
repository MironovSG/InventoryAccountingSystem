package com.systemtmc.inventory.service;

import com.systemtmc.inventory.dto.MaterialDTO;
import com.systemtmc.inventory.model.entity.Material;
import com.systemtmc.inventory.model.entity.MaterialCategory;
import com.systemtmc.inventory.model.enums.WarehouseType;
import com.systemtmc.inventory.repository.MaterialCategoryRepository;
import com.systemtmc.inventory.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с ТМЦ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialService {
    
    private final MaterialRepository materialRepository;
    private final MaterialCategoryRepository categoryRepository;
    private final AuditService auditService;
    
    @Transactional(readOnly = true)
    public List<MaterialDTO> getAllMaterials() {
        return materialRepository.findByDeletedFalse().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MaterialDTO> getActiveMaterials() {
        return materialRepository.findAllActiveMaterials().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public MaterialDTO getMaterialById(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Материал не найден с ID: " + id));
        return convertToDTO(material);
    }
    
    @Transactional(readOnly = true)
    public MaterialDTO getMaterialByArticle(String article) {
        Material material = materialRepository.findByArticle(article)
                .orElseThrow(() -> new RuntimeException("Материал не найден с артикулом: " + article));
        return convertToDTO(material);
    }
    
    @Transactional(readOnly = true)
    public List<MaterialDTO> getMaterialsByCategory(Long categoryId) {
        return materialRepository.findActiveMaterialsByCategory(categoryId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MaterialDTO> getInStockMaterials() {
        return materialRepository.findInStockMaterials().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MaterialDTO> getLowStockMaterials() {
        return materialRepository.findLowStockMaterials().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MaterialDTO> getCriticalStockMaterials() {
        return materialRepository.findCriticalStockMaterials().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MaterialDTO> getOutOfStockMaterials() {
        return materialRepository.findOutOfStockMaterials().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MaterialDTO> searchMaterials(String search) {
        return materialRepository.searchMaterials(search).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public MaterialDTO createMaterial(MaterialDTO materialDTO) {
        if (materialRepository.existsByArticle(materialDTO.getArticle())) {
            throw new RuntimeException("Материал с таким артикулом уже существует");
        }
        
        Material material = new Material();
        material.setArticle(materialDTO.getArticle());
        material.setName(materialDTO.getName());
        material.setDescription(materialDTO.getDescription());
        material.setUnitOfMeasure(materialDTO.getUnitOfMeasure());
        material.setCurrentQuantity(materialDTO.getCurrentQuantity() != null ? 
                materialDTO.getCurrentQuantity() : BigDecimal.ZERO);
        material.setMinQuantity(materialDTO.getMinQuantity());
        material.setMaxQuantity(materialDTO.getMaxQuantity());
        material.setCriticalQuantity(materialDTO.getCriticalQuantity());
        material.setStorageLocation(materialDTO.getStorageLocation());
        material.setStorageConditions(materialDTO.getStorageConditions());
        material.setUnitPrice(materialDTO.getUnitPrice());
        material.setManufacturer(materialDTO.getManufacturer());
        material.setSupplier(materialDTO.getSupplier());
        material.setBarcode(materialDTO.getBarcode());
        material.setImageUrl(materialDTO.getImageUrl());
        material.setActive(materialDTO.getActive() != null ? materialDTO.getActive() : true);
        material.setWarehouseType(String.valueOf(materialDTO.getWarehouseType()));
        
        MaterialCategory category = categoryRepository.findById(materialDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        material.setCategory(category);
        
        Material savedMaterial = materialRepository.save(material);
        auditService.logAction("CREATE_MATERIAL", "Material", savedMaterial.getId(), 
                "Создан материал: " + savedMaterial.getArticle() + " - " + savedMaterial.getName());
        
        log.info("Создан материал: {} - {}", savedMaterial.getArticle(), savedMaterial.getName());
        return convertToDTO(savedMaterial);
    }
    
    @Transactional
    public MaterialDTO updateMaterial(Long id, MaterialDTO materialDTO) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));
        
        if (!material.getArticle().equals(materialDTO.getArticle()) && 
            materialRepository.existsByArticle(materialDTO.getArticle())) {
            throw new RuntimeException("Материал с таким артикулом уже существует");
        }
        
        String oldValue = material.getName() + " (остаток: " + material.getCurrentQuantity() + ")";
        
        material.setArticle(materialDTO.getArticle());
        material.setName(materialDTO.getName());
        material.setDescription(materialDTO.getDescription());
        material.setUnitOfMeasure(materialDTO.getUnitOfMeasure());
        material.setMinQuantity(materialDTO.getMinQuantity());
        material.setMaxQuantity(materialDTO.getMaxQuantity());
        material.setCriticalQuantity(materialDTO.getCriticalQuantity());
        material.setStorageLocation(materialDTO.getStorageLocation());
        material.setStorageConditions(materialDTO.getStorageConditions());
        material.setUnitPrice(materialDTO.getUnitPrice());
        material.setManufacturer(materialDTO.getManufacturer());
        material.setSupplier(materialDTO.getSupplier());
        material.setBarcode(materialDTO.getBarcode());
        material.setImageUrl(materialDTO.getImageUrl());
        material.setActive(materialDTO.getActive());
        material.setWarehouseType(String.valueOf(materialDTO.getWarehouseType()));
        
        if (!material.getCategory().getId().equals(materialDTO.getCategoryId())) {
            MaterialCategory category = categoryRepository.findById(materialDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена"));
            material.setCategory(category);
        }
        
        Material updatedMaterial = materialRepository.save(material);
        
        String newValue = updatedMaterial.getName() + " (остаток: " + updatedMaterial.getCurrentQuantity() + ")";
        auditService.logAction("UPDATE_MATERIAL", "Material", updatedMaterial.getId(), 
                "Обновлен материал: " + updatedMaterial.getArticle(), oldValue, newValue);
        
        log.info("Обновлен материал: {} - {}", updatedMaterial.getArticle(), updatedMaterial.getName());
        return convertToDTO(updatedMaterial);
    }
    
    @Transactional
    public void deleteMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));
        
        material.setDeleted(true);
        material.setActive(false);
        materialRepository.save(material);
        
        auditService.logAction("DELETE_MATERIAL", "Material", id, 
                "Удален материал: " + material.getArticle() + " - " + material.getName());
        
        log.info("Удален материал: {} - {}", material.getArticle(), material.getName());
    }
    
    @Transactional
    public void updateQuantity(Long id, BigDecimal quantity) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));
        
        BigDecimal oldQuantity = material.getCurrentQuantity();
        material.setCurrentQuantity(quantity);
        materialRepository.save(material);
        
        log.info("Обновлен остаток материала {} с {} на {}", 
                material.getArticle(), oldQuantity, quantity);
    }
    
    private MaterialDTO convertToDTO(Material material) {
        MaterialDTO dto = MaterialDTO.builder()
                .id(material.getId())
                .article(material.getArticle())
                .name(material.getName())
                .description(material.getDescription())
                .categoryId(material.getCategory().getId())
                .categoryName(material.getCategory().getName())
                .unitOfMeasure(material.getUnitOfMeasure())
                .currentQuantity(material.getCurrentQuantity())
                .minQuantity(material.getMinQuantity())
                .maxQuantity(material.getMaxQuantity())
                .criticalQuantity(material.getCriticalQuantity())
                .storageLocation(material.getStorageLocation())
                .storageConditions(material.getStorageConditions())
                .unitPrice(material.getUnitPrice())
                .manufacturer(material.getManufacturer())
                .supplier(material.getSupplier())
                .barcode(material.getBarcode())
                .imageUrl(material.getImageUrl())
                .active(material.getActive())
                .stockLevel(material.calculateStockLevel())
                .warehouseType(WarehouseType.valueOf(material.getWarehouseType()))
                .createdAt(material.getCreatedAt())
                .updatedAt(material.getUpdatedAt())
                .build();
        
        return dto;
    }
}
