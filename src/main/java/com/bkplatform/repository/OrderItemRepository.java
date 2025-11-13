package com.bkplatform.repository;
import com.bkplatform.model.OrderItem;
import com.bkplatform.model.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {}
