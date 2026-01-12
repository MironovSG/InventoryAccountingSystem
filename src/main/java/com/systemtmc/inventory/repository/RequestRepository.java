package com.systemtmc.inventory.repository;

import com.systemtmc.inventory.model.entity.Request;
import com.systemtmc.inventory.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с заявками
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {
    
    Optional<Request> findByRequestNumber(String requestNumber);
    
    List<Request> findByStatus(RequestStatus status);
    
    List<Request> findByRequesterId(Long requesterId);
    
    List<Request> findByDepartmentId(Long departmentId);
    
    @Query("SELECT r FROM Request r WHERE r.deleted = false ORDER BY r.createdAt DESC")
    List<Request> findAllRequests();
    
    @Query("SELECT r FROM Request r WHERE r.deleted = false AND r.status = :status ORDER BY r.createdAt DESC")
    List<Request> findRequestsByStatus(@Param("status") RequestStatus status);
    
    @Query("SELECT r FROM Request r WHERE r.deleted = false AND r.requester.id = :userId ORDER BY r.createdAt DESC")
    List<Request> findRequestsByUser(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Request r WHERE r.deleted = false AND r.department.id = :departmentId ORDER BY r.createdAt DESC")
    List<Request> findRequestsByDepartment(@Param("departmentId") Long departmentId);
    
    @Query("SELECT r FROM Request r WHERE r.deleted = false AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<Request> findRequestsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT r FROM Request r WHERE r.deleted = false AND r.department.id = :departmentId AND " +
           "r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<Request> findRequestsByDepartmentAndDateRange(
        @Param("departmentId") Long departmentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT r FROM Request r WHERE r.deleted = false AND " +
           "(LOWER(r.requestNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.purpose) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Request> searchRequests(@Param("search") String search);
    
    @Query("SELECT COUNT(r) FROM Request r WHERE r.deleted = false AND r.status = :status")
    long countByStatus(@Param("status") RequestStatus status);
    
    @Query("SELECT COUNT(r) FROM Request r WHERE r.deleted = false AND r.department.id = :departmentId AND r.status = :status")
    long countByDepartmentAndStatus(@Param("departmentId") Long departmentId, @Param("status") RequestStatus status);
    
    boolean existsByRequestNumber(String requestNumber);
}
