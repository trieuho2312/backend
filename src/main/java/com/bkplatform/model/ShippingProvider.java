package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="shipping_provider")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShippingProvider {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long providerId;
    @Column(unique=true, nullable=false, length=100)
    private String providerName;
    @Column(length=20)
    private String contactNumber;
}
