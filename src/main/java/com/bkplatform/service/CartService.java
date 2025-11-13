package com.bkplatform.service;

import com.bkplatform.dto.AddCartItemRequest;
import com.bkplatform.exception.InsufficientStockException;
import com.bkplatform.exception.ResourceNotFoundException;
import com.bkplatform.model.*;
import com.bkplatform.repository.CartItemRepository;
import com.bkplatform.repository.CartRepository;
import com.bkplatform.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    private static final int MAX_QUANTITY_PER_ITEM = 999;

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("Creating new cart for user: {}", user.getUsername());
                    return cartRepository.save(Cart.builder().user(user).build());
                });
    }

    public List<CartItem> getCart(User user) {
        Cart cart = getOrCreateCart(user);
        // ✅ FIX: Use findByCart instead of findAll().filter()
        return cartItemRepository.findByCart(cart);
    }

    @Transactional
    public CartItem addItem(User user, AddCartItemRequest req) {
        // ✅ Validate product exists and has stock
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        int requestedQty = req.getQuantity() != null ? req.getQuantity() : 1;

        // ✅ Validate quantity
        if (requestedQty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (requestedQty > MAX_QUANTITY_PER_ITEM) {
            throw new IllegalArgumentException("Quantity exceeds maximum limit: " + MAX_QUANTITY_PER_ITEM);
        }

        Cart cart = getOrCreateCart(user);

        // ✅ Build composite key
        CartItemId id = new CartItemId();
        id.setCartId(cart.getCartId());
        id.setProductId(product.getProductId());

        // ✅ Get existing or create new
        CartItem item = cartItemRepository.findById(id)
                .orElse(CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(0)
                        .build());

        int newQuantity = item.getQuantity() + requestedQty;

        // ✅ Check stock availability
        if (newQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested: %d",
                            product.getStockQuantity(), newQuantity)
            );
        }

        if (newQuantity > MAX_QUANTITY_PER_ITEM) {
            throw new IllegalArgumentException("Total quantity exceeds maximum limit");
        }

        item.setQuantity(newQuantity);

        log.info("Added {} items of product {} to cart for user {}",
                requestedQty, product.getProductId(), user.getUsername());

        return cartItemRepository.save(item);
    }

    @Transactional
    public CartItem updateItem(User user, Long productId, int quantity) {
        // ✅ Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new IllegalArgumentException("Quantity exceeds maximum limit");
        }

        Cart cart = getOrCreateCart(user);

        CartItemId id = new CartItemId();
        id.setCartId(cart.getCartId());
        id.setProductId(productId);

        CartItem item = cartItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        // ✅ Check stock
        if (quantity > item.getProduct().getStockQuantity()) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested: %d",
                            item.getProduct().getStockQuantity(), quantity)
            );
        }

        item.setQuantity(quantity);

        log.info("Updated cart item quantity to {} for user {}", quantity, user.getUsername());

        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(User user, Long productId) {
        Cart cart = getOrCreateCart(user);

        CartItemId id = new CartItemId();
        id.setCartId(cart.getCartId());
        id.setProductId(productId);

        // ✅ Check exists before delete
        if (!cartItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        cartItemRepository.deleteById(id);

        log.info("Removed product {} from cart for user {}", productId, user.getUsername());
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartItemRepository.deleteByCart(cart);

        log.info("Cleared cart for user {}", user.getUsername());
    }

    public long getCartItemCount(User user) {
        Cart cart = getOrCreateCart(user);
        return cartItemRepository.countByCart(cart);
    }
}