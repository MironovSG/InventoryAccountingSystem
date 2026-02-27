package com.systemtmc.inventory.repository;

import com.systemtmc.inventory.model.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с ТМЦ
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {
    
    Optional<Material> findByArticle(String article);
    
    List<Material> findByCategoryId(Long categoryId);
    
    List<Material> findByActiveTrue();
    
    List<Material> findByDeletedFalse();
    
    @Query("SELECT m FROM Material m WHERE m.deleted = false AND m.active = true")
    List<Material> findAllActiveMaterials();
    
    @Query("SELECT m FROM Material m WHERE m.deleted = false AND m.active = true AND m.category.id = :categoryId")
    List<Material> findActiveMaterialsByCategory(@Param("categoryId") Long categoryId);
    
    @Query("SELECT m FROM Material m WHERE m.deleted = false AND m.active = true AND " +
           "m.currentQuantity > 0")
    List<Material> findInStockMaterials();
    
    @Query("SELECT m FROM Material m WHERE m.deleted = false AND m.active = true AND " +
           "m.currentQuantity <= m.minQuantity")
    List<Material> findLowStockMaterials();
    
    @Query("SELECT m FROM Material m WHERE m.deleted = false AND m.active = true AND " +
           "m.currentQuantity <= m.criticalQuantity")
    List<Material> findCriticalStockMaterials();
    
    @Query("SELECT m FROM Material m WHERE m.deleted = false AND m.active = true AND " +
           "m.currentQuantity = 0")
    List<Material> findOutOfStockMaterials();
    
    @Query("SELECT m FROM Material m WHERE m.deleted = false AND " +
           "(LOWER(m.article) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Material> searchMaterials(@Param("search") String search);
    
    @Query("SELECT m FROM Material m WHERE m.deleted = false AND m.active = true AND " +
           "m.category.id = :categoryId AND " +
           "(LOWER(m.article) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Material> searchMaterialsByCategory(@Param("categoryId") Long categoryId, @Param("search") String search);
    
    boolean existsByArticle(String article);
    
    @Query("SELECT COUNT(m) FROM Material m WHERE m.deleted = false AND m.active = true")
    long countActiveMaterials();
    
    @Query("SELECT COUNT(m) FROM Material m WHERE m.deleted = false AND m.active = true AND m.currentQuantity > 0")
    long countInStockMaterials();
    
    @Query("SELECT SUM(m.currentQuantity * m.unitPrice) FROM Material m WHERE m.deleted = false AND m.active = true")
    BigDecimal calculateTotalInventoryValue();
}
