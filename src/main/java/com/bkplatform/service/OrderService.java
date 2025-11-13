package com.bkplatform.service;

import com.bkplatform.dto.CheckoutResponse;
import com.bkplatform.exception.EmptyCartException;
import com.bkplatform.exception.InsufficientStockException;
import com.bkplatform.exception.ResourceNotFoundException;
import com.bkplatform.exception.UnauthorizedException;
import com.bkplatform.model.*;
import com.bkplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CheckoutResponse checkout(User user) {
        log.info("Starting checkout for user: {}", user.getUsername());

        // ✅ Get cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        // ✅ FIX: Use findByCart instead of findAll().filter()
        List<CartItem> items = cartItemRepository.findByCart(cart);

        // ✅ Validate cart not empty
        if (items.isEmpty()) {
            throw new EmptyCartException("Cannot checkout with empty cart");
        }

        // ✅ Validate stock and calculate total
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : items) {
            Product product = item.getProduct();
            int requestedQty = item.getQuantity();

            // ✅ Validate quantity
            if (requestedQty <= 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid quantity for product %s", product.getName())
                );
            }

            // ✅ Check stock availability
            if (product.getStockQuantity() < requestedQty) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                                product.getName(),
                                product.getStockQuantity(),
                                requestedQty
                        )
                );
            }

            // Calculate subtotal
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(requestedQty));
            total = total.add(subtotal);
        }

        // ✅ Create order
        Order order = Order.builder()
                .user(user)
                .orderCost(total)
                .build();
        order = orderRepository.save(order);

        log.info("Created order {} with total cost: {}", order.getOrderId(), total);

        // ✅ Create order items and update stock
        for (CartItem item : items) {
            Product product = item.getProduct();

            // Create order item with price snapshot
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .priceSnapshot(product.getPrice())
                    .build();
            orderItemRepository.save(orderItem);

            // ✅ CRITICAL FIX: Reduce stock quantity
            int newStock = product.getStockQuantity() - item.getQuantity();
            product.setStockQuantity(newStock);
            productRepository.save(product);

            log.info("Updated stock for product {}: {} -> {}",
                    product.getProductId(),
                    product.getStockQuantity() + item.getQuantity(),
                    newStock
            );
        }

        // ✅ Clear cart after successful checkout
        cartItemRepository.deleteByCart(cart);

        log.info("Checkout completed successfully for order {}", order.getOrderId());

        // Build response
        CheckoutResponse response = new CheckoutResponse();
        response.setOrderId(order.getOrderId());
        response.setStatus("SUCCESS");
        return response;
    }

    /**
     * Get all orders for a user
     */
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    /**
     * Get order details by ID
     */
    public Order getOrderById(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // ✅ Verify user owns this order
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException("You don't have permission to view this order");
        }

        return order;
    }

    /**
     * Cancel order (if not yet processed)
     */
    @Transactional
    public void cancelOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // ✅ Verify user owns this order
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException("You don't have permission to cancel this order");
        }

        // ✅ Check if order can be cancelled (add your business logic here)
        // For example: only allow cancel within 24 hours, or if status is PENDING

        // ✅ Restore stock
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        // Delete order (or update status to CANCELLED)
        orderRepository.delete(order);

        log.info("Order {} cancelled by user {}", orderId, user.getUsername());
    }
}