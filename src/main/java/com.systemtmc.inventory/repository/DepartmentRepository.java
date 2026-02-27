package com.systemtmc.inventory.repository;

import com.systemtmc.inventory.model.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с подразделениями
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
    
    Optional<Department> findByCode(String code);
    
    List<Department> findByParentId(Long parentId);
    
    List<Department> findByParentIsNull();
    
    List<Department> findByActiveTrue();
    
    List<Department> findByDeletedFalse();
    
    @Query("SELECT d FROM Department d WHERE d.deleted = false AND d.active = true")
    List<Department> findAllActiveDepartments();
    
    @Query("SELECT d FROM Department d WHERE d.deleted = false AND " +
           "(LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Department> searchDepartments(@Param("search") String search);
    
    boolean existsByCode(String code);
}
