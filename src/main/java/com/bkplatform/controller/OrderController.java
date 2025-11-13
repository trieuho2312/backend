package com.bkplatform.controller;

import com.bkplatform.dto.CheckoutResponse;
import com.bkplatform.model.Order;
import com.bkplatform.model.User;
import com.bkplatform.repository.OrderRepository;
import com.bkplatform.repository.UserRepository;
import com.bkplatform.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(orderService.checkout(user));
    }

    @GetMapping
    public ResponseEntity<List<Order>> myOrders(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(orderRepository.findByUserOrderByOrderDateDesc(user));
    }
}