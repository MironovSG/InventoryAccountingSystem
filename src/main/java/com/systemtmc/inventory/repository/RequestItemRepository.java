package com.systemtmc.inventory.repository;

import com.systemtmc.inventory.model.entity.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с позициями заявок
 */
@Repository
public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {
    
    List<RequestItem> findByRequestId(Long requestId);
    
    List<RequestItem> findByMaterialId(Long materialId);
    
    @Query("SELECT ri FROM RequestItem ri WHERE ri.request.id = :requestId ORDER BY ri.id")
    List<RequestItem> findItemsByRequest(@Param("requestId") Long requestId);
    
    @Query("SELECT ri FROM RequestItem ri WHERE ri.material.id = :materialId ORDER BY ri.createdAt DESC")
    List<RequestItem> findItemsByMaterial(@Param("materialId") Long materialId);
}
