package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne(optional=false) @JoinColumn(name="shop_id")
    private Shop shop;

    @Column(nullable=false, length=150)
    private String name;

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal price;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable=false)
    private Integer stockQuantity = 0;

    @Column(nullable=false)
    private Instant createdDate = Instant.now();

    @ManyToOne @JoinColumn(name="category_id")
    private Category category;
}
