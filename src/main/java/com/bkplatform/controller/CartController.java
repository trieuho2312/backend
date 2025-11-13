package com.bkplatform.controller;

import com.bkplatform.dto.AddCartItemRequest;
import com.bkplatform.dto.UpdateCartItemRequest;
import com.bkplatform.model.User;
import com.bkplatform.repository.UserRepository;
import com.bkplatform.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItem(@AuthenticationPrincipal UserDetails principal,
                                     @Valid @RequestBody AddCartItemRequest req) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        cartService.addItem(user, req);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<?> updateItem(@AuthenticationPrincipal UserDetails principal,
                                        @PathVariable Long productId,
                                        @Valid @RequestBody UpdateCartItemRequest req) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        cartService.updateItem(user, productId, req.getQuantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> removeItem(@AuthenticationPrincipal UserDetails principal,
                                        @PathVariable Long productId) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        cartService.removeItem(user, productId);
        return ResponseEntity.ok().build();
    }
}
