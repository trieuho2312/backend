package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="product_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;
    @ManyToOne(optional=false) @JoinColumn(name="product_id")
    private Product product;
    @Column(columnDefinition="text", nullable=false)
    private String imageUrl;
    @Column(length=50)
    private String imageType;
    @Column(columnDefinition="text")
    private String description;
}
