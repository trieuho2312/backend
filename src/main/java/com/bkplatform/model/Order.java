package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;
    @Column(nullable=false)
    private Instant orderDate = Instant.now();
    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal orderCost;
}
