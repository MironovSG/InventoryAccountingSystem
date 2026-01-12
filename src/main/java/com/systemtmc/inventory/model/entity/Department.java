package com.systemtmc.inventory.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность подразделения
 */
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_department_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {
    
    @NotBlank(message = "Код подразделения обязателен")
    @Size(max = 20, message = "Код подразделения не может быть длиннее 20 символов")
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;
    
    @NotBlank(message = "Название подразделения обязательно")
    @Size(max = 200, message = "Название не может быть длиннее 200 символов")
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Department> children = new ArrayList<>();
    
    @OneToMany(mappedBy = "department")
    @Builder.Default
    private List<User> users = new ArrayList<>();
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
