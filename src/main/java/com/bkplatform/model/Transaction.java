package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    @OneToOne @JoinColumn(name="order_id", unique = true, nullable = false)
    private Order order;
    @Column(nullable=false, length=50)
    private String transactionMethod;
    @Column(nullable=false, length=30)
    private String transactionStatus = "pending";
    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal totalCost;
    @Column(precision=12, scale=2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(nullable=false)
    private Instant transactionDate = Instant.now();
}
