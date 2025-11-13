package com.bkplatform.repository;
import com.bkplatform.model.Shop;
import com.bkplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByOwner(User owner);
}
