package com.example.cafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "promotions")
public class Promotion {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ Tự động set thời gian khi tạo và cập nhật
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }



    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
@JoinTable(
    name = "promotion_products",
    joinColumns = @JoinColumn(name = "promotion_id"),
    inverseJoinColumns = @JoinColumn(name = "product_id")
)
@JsonIgnoreProperties("promotions")
private Set<Product> products;


}