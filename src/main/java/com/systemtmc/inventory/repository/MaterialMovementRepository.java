package com.systemtmc.inventory.repository;

import com.systemtmc.inventory.model.entity.MaterialMovement;
import com.systemtmc.inventory.model.enums.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с движениями ТМЦ
 */
@Repository
public interface MaterialMovementRepository extends JpaRepository<MaterialMovement, Long>, JpaSpecificationExecutor<MaterialMovement> {
    
    List<MaterialMovement> findByMaterialId(Long materialId);
    
    List<MaterialMovement> findByMovementType(MovementType movementType);
    
    List<MaterialMovement> findByRequestId(Long requestId);
    
    List<MaterialMovement> findByUserId(Long userId);
    
    List<MaterialMovement> findByDepartmentId(Long departmentId);
    
    @Query("SELECT mm FROM MaterialMovement mm WHERE mm.material.id = :materialId ORDER BY mm.createdAt DESC")
    List<MaterialMovement> findMovementsByMaterial(@Param("materialId") Long materialId);
    
    @Query("SELECT mm FROM MaterialMovement mm WHERE mm.material.id = :materialId AND " +
           "mm.createdAt BETWEEN :startDate AND :endDate ORDER BY mm.createdAt DESC")
    List<MaterialMovement> findMovementsByMaterialAndDateRange(
        @Param("materialId") Long materialId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT mm FROM MaterialMovement mm WHERE mm.department.id = :departmentId AND " +
           "mm.createdAt BETWEEN :startDate AND :endDate ORDER BY mm.createdAt DESC")
    List<MaterialMovement> findMovementsByDepartmentAndDateRange(
        @Param("departmentId") Long departmentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT mm FROM MaterialMovement mm WHERE mm.movementType = :movementType AND " +
           "mm.createdAt BETWEEN :startDate AND :endDate ORDER BY mm.createdAt DESC")
    List<MaterialMovement> findMovementsByTypeAndDateRange(
        @Param("movementType") MovementType movementType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT mm FROM MaterialMovement mm WHERE " +
           "mm.createdAt BETWEEN :startDate AND :endDate ORDER BY mm.createdAt DESC")
    List<MaterialMovement> findMovementsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
