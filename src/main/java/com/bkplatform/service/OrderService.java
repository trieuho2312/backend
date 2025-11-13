package com.bkplatform.service;

import com.bkplatform.dto.CheckoutResponse;
import com.bkplatform.model.*;
import com.bkplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public CheckoutResponse checkout(User user) {
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        List<CartItem> items = cartItemRepository.findAll().stream().filter(ci -> ci.getCart().getCartId().equals(cart.getCartId())).toList();
        if (items.isEmpty()) throw new IllegalStateException("Giỏ hàng trống");
        BigDecimal total = items.stream().map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = orderRepository.save(Order.builder().user(user).orderCost(total).build());
        for (CartItem i : items) {
            orderItemRepository.save(OrderItem.builder()
                    .order(order)
                    .product(i.getProduct())
                    .quantity(i.getQuantity())
                    .priceSnapshot(i.getProduct().getPrice())
                    .build());
        }
        cartItemRepository.deleteAll(items);
        CheckoutResponse resp = new CheckoutResponse();
        resp.setOrderId(order.getOrderId());
        return resp;
    }
}
