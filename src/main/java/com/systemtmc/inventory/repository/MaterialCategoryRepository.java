package com.systemtmc.inventory.repository;

import com.systemtmc.inventory.model.entity.MaterialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с категориями ТМЦ
 */
@Repository
public interface MaterialCategoryRepository extends JpaRepository<MaterialCategory, Long>, JpaSpecificationExecutor<MaterialCategory> {
    
    Optional<MaterialCategory> findByCode(String code);
    
    List<MaterialCategory> findByParentId(Long parentId);
    
    List<MaterialCategory> findByParentIsNull();
    
    List<MaterialCategory> findByActiveTrue();
    
    List<MaterialCategory> findByDeletedFalse();
    
    @Query("SELECT mc FROM MaterialCategory mc WHERE mc.deleted = false AND mc.active = true")
    List<MaterialCategory> findAllActiveCategories();
    
    @Query("SELECT mc FROM MaterialCategory mc WHERE mc.deleted = false AND " +
           "(LOWER(mc.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(mc.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<MaterialCategory> searchCategories(@Param("search") String search);
    
    boolean existsByCode(String code);
}
