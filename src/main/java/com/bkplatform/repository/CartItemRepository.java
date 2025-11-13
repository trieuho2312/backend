
package com.bkplatform.repository;

import com.bkplatform.model.Cart;
import com.bkplatform.model.CartItem;
import com.bkplatform.model.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

    // ✅ FIX: Query directly instead of findAll().filter()
    List<CartItem> findByCart(Cart cart);

    // ✅ Additional useful methods
    void deleteByCart(Cart cart);

    long countByCart(Cart cart);
}
