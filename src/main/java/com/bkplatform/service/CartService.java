package com.bkplatform.service;

import com.bkplatform.dto.AddCartItemRequest;
import com.bkplatform.model.*;
import com.bkplatform.repository.CartItemRepository;
import com.bkplatform.repository.CartRepository;
import com.bkplatform.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    public List<CartItem> getCart(User user) {
        Cart cart = getOrCreateCart(user);
        return cartItemRepository.findAll().stream().filter(ci -> ci.getCart().getCartId().equals(cart.getCartId())).toList();
    }

    @Transactional
    public void addItem(User user, AddCartItemRequest req) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(req.getProductId()).orElseThrow();
        CartItemId id = new CartItemId();
        id.setCartId(cart.getCartId());
        id.setProductId(product.getProductId());
        CartItem item = cartItemRepository.findById(id).orElse(CartItem.builder().cart(cart).product(product).quantity(0).build());
        item.setQuantity(item.getQuantity() + (req.getQuantity() == null ? 1 : req.getQuantity()));
        cartItemRepository.save(item);
    }

    @Transactional
    public void updateItem(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);
        CartItemId id = new CartItemId();
        id.setCartId(cart.getCartId());
        id.setProductId(productId);
        CartItem item = cartItemRepository.findById(id).orElseThrow();
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        CartItemId id = new CartItemId();
        id.setCartId(cart.getCartId());
        id.setProductId(productId);
        cartItemRepository.deleteById(id);
    }
}
