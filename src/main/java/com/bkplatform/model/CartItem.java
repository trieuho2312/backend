package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @EmbeddedId
    private CartItemId id;

    @ManyToOne
    @MapsId("cartId") // ánh xạ cartId trong CartItemId
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @MapsId("productId") // ánh xạ productId trong CartItemId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;
}
