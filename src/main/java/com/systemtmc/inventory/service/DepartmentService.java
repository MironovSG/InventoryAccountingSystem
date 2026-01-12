package com.systemtmc.inventory.service;

import com.systemtmc.inventory.dto.DepartmentDTO;
import com.systemtmc.inventory.model.entity.Department;
import com.systemtmc.inventory.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с подразделениями
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;
    
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findByDeletedFalse().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getActiveDepartments() {
        return departmentRepository.findAllActiveDepartments().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Подразделение не найдено с ID: " + id));
        return convertToDTO(department);
    }
    
    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        if (departmentRepository.existsByCode(departmentDTO.getCode())) {
            throw new RuntimeException("Подразделение с таким кодом уже существует");
        }
        
        Department department = new Department();
        department.setCode(departmentDTO.getCode());
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setActive(departmentDTO.getActive() != null ? departmentDTO.getActive() : true);
        
        if (departmentDTO.getParentId() != null) {
            Department parent = departmentRepository.findById(departmentDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Родительское подразделение не найдено"));
            department.setParent(parent);
        }
        
        Department savedDepartment = departmentRepository.save(department);
        auditService.logAction("CREATE_DEPARTMENT", "Department", savedDepartment.getId(), 
                "Создано подразделение: " + savedDepartment.getName());
        
        log.info("Создано подразделение: {}", savedDepartment.getName());
        return convertToDTO(savedDepartment);
    }
    
    @Transactional
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Подразделение не найдено"));
        
        if (!department.getCode().equals(departmentDTO.getCode()) && 
            departmentRepository.existsByCode(departmentDTO.getCode())) {
            throw new RuntimeException("Подразделение с таким кодом уже существует");
        }
        
        department.setCode(departmentDTO.getCode());
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setActive(departmentDTO.getActive());
        
        Department updatedDepartment = departmentRepository.save(department);
        auditService.logAction("UPDATE_DEPARTMENT", "Department", updatedDepartment.getId(), 
                "Обновлено подразделение: " + updatedDepartment.getName());
        
        log.info("Обновлено подразделение: {}", updatedDepartment.getName());
        return convertToDTO(updatedDepartment);
    }
    
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Подразделение не найдено"));
        
        department.setDeleted(true);
        department.setActive(false);
        departmentRepository.save(department);
        
        auditService.logAction("DELETE_DEPARTMENT", "Department", id, 
                "Удалено подразделение: " + department.getName());
        
        log.info("Удалено подразделение: {}", department.getName());
    }
    
    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = DepartmentDTO.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .description(department.getDescription())
                .active(department.getActive())
                .createdAt(department.getCreatedAt())
                .build();
        
        if (department.getParent() != null) {
            dto.setParentId(department.getParent().getId());
            dto.setParentName(department.getParent().getName());
        }
        
        return dto;
    }
}
