package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name="product_review", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id","user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productReviewId;
    @ManyToOne(optional=false) @JoinColumn(name="product_id")
    private Product product;
    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;
    @Column(columnDefinition="text")
    private String content;
    @Column(nullable=false)
    private Integer rating;
    @Column(nullable=false)
    private Instant reviewDate = Instant.now();
}
