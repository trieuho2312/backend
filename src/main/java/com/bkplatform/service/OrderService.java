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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @Transactional
    public CheckoutResponse checkout(User user) {
        log.info("Starting checkout for user: {}", user.getUsername());

        // ✅ Get cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        // ✅ Get cart items
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

            // ✅ CRITICAL: Reduce stock quantity
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

        // ✅ FIX: Send order confirmation email
        try {
            String formattedAmount = formatCurrency(total);
            emailService.sendOrderConfirmationEmail(
                    user.getEmail(),
                    order.getOrderId(),
                    formattedAmount
            );
            log.info("Order confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            // Don't fail checkout if email fails
            log.error("Failed to send order confirmation email", e);
        }

        // Build response
        CheckoutResponse response = new CheckoutResponse();
        response.setOrderId(order.getOrderId());
        response.setStatus("SUCCESS");
        return response;
    }

    /**
     * ✅ Format currency for Vietnamese Dong
     */
    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
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
     * ✅ FIX: Add proper validation and logging
     */
    @Transactional
    public void cancelOrder(Long orderId, User user) {
        log.info("Cancelling order {} by user {}", orderId, user.getUsername());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // ✅ Verify user owns this order
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException("You don't have permission to cancel this order");
        }

        // ✅ Check if order can be cancelled (example: within 1 hour of order)
        long hoursSinceOrder = java.time.Duration.between(
                order.getOrderDate().toInstant(),
                java.time.Instant.now()
        ).toHours();

        if (hoursSinceOrder > 1) {
            throw new IllegalStateException("Order can only be cancelled within 1 hour of placement");
        }

        // ✅ Restore stock
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            int restoredStock = product.getStockQuantity() + item.getQuantity();
            product.setStockQuantity(restoredStock);
            productRepository.save(product);

            log.info("Restored stock for product {}: {} -> {}",
                    product.getProductId(),
                    product.getStockQuantity() - item.getQuantity(),
                    restoredStock
            );
        }

        // Delete order
        orderRepository.delete(order);

        log.info("Order {} cancelled successfully by user {}", orderId, user.getUsername());
    }
}