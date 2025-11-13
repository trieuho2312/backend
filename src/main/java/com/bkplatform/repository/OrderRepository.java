package com.bkplatform.repository;
import com.bkplatform.model.Order;
import com.bkplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
}
