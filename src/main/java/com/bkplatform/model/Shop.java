package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="shop")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shopId;
    @ManyToOne(optional = false) @JoinColumn(name="owner_id")
    private User owner;
    @Column(nullable=false, length=100)
    private String name;
    @Column(precision = 2, scale = 1)
    private java.math.BigDecimal averageRating;
    @ManyToOne @JoinColumn(name="address_id")
    private AddressBook address;
}
