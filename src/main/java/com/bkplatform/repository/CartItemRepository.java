package com.bkplatform.repository;
import com.bkplatform.model.CartItem;
import com.bkplatform.model.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {}
