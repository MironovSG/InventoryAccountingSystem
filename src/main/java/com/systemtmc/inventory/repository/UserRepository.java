package com.systemtmc.inventory.repository;

import com.systemtmc.inventory.model.entity.User;
import com.systemtmc.inventory.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByDepartmentId(Long departmentId);
    
    List<User> findByActiveTrue();
    
    List<User> findByDeletedFalse();
    
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.active = true")
    List<User> findAllActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.active = true AND u.role = :role")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchUsers(@Param("search") String search);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
