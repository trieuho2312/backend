package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="cart")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;
    @OneToOne @JoinColumn(name="user_id", unique = true, nullable = false)
    private User user;
}
