package com.systemtmc.inventory.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность категории ТМЦ
 */
@Entity
@Table(name = "material_categories", indexes = {
    @Index(name = "idx_category_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialCategory extends BaseEntity {
    
    @NotBlank(message = "Код категории обязателен")
    @Size(max = 20, message = "Код категории не может быть длиннее 20 символов")
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;
    
    @NotBlank(message = "Название категории обязательно")
    @Size(max = 200, message = "Название не может быть длиннее 200 символов")
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MaterialCategory parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MaterialCategory> children = new ArrayList<>();
    
    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Material> materials = new ArrayList<>();
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
