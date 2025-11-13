package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="shipments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipmentId;
    @OneToOne @JoinColumn(name="order_id", unique = true, nullable = false)
    private Order order;
    @ManyToOne(optional=false) @JoinColumn(name="provider_id")
    private ShippingProvider provider;
    private LocalDate deliverDate;
    @Column(precision=12, scale=2)
    private BigDecimal shippingCost;
    @Column(columnDefinition="text")
    private String receiverAddress;
    @Column(columnDefinition="text")
    private String pickUpAddress;
}
