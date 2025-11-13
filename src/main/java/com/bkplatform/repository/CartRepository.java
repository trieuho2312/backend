package com.bkplatform.repository;
import com.bkplatform.model.Cart;
import com.bkplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
