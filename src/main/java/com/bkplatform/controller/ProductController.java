package com.bkplatform.controller;

import com.bkplatform.dto.CreateProductRequest;
import com.bkplatform.dto.UpdateProductRequest;
import com.bkplatform.model.Product;
import com.bkplatform.model.User;
import com.bkplatform.repository.UserRepository;
import com.bkplatform.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    /**
     * ✅ PUBLIC - Search products (NO TOKEN REQUIRED)
     */
    @GetMapping
    public ResponseEntity<Page<Product>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Page<Product> products = productService.search(search, categoryId, sort, page, size);
        return ResponseEntity.ok(products);
    }

    /**
     * ✅ PUBLIC - Get product by ID (NO TOKEN REQUIRED)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ✅ PROTECTED - Create product (TOKEN REQUIRED)
     * FIX: Better error handling và logging
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateProductRequest req) {

        try {
            User owner = userRepository.findByUsername(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Product product = productService.create(req, owner);

            log.info("Product created: {} by user: {}", product.getProductId(), owner.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(product);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid product data: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to create product"
            ));
        }
    }

    /**
     * ✅ PROTECTED - Update product (TOKEN REQUIRED)
     * FIX: Better error handling
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest req) {

        try {
            User owner = userRepository.findByUsername(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return productService.update(id, req, owner)
                    .map(product -> {
                        log.info("Product updated: {} by user: {}", id, owner.getUsername());
                        return ResponseEntity.ok(product);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (SecurityException e) {
            log.warn("Unauthorized product update attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", "error",
                    "message", "You don't have permission to update this product"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error updating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to update product"
            ));
        }
    }

    /**
     * ✅ PROTECTED - Delete product (TOKEN REQUIRED)
     * FIX: Better error handling
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {

        try {
            User owner = userRepository.findByUsername(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean deleted = productService.delete(id, owner);

            if (deleted) {
                log.info("Product deleted: {} by user: {}", id, owner.getUsername());
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Product deleted successfully"
                ));
            }

            return ResponseEntity.notFound().build();

        } catch (SecurityException e) {
            log.warn("Unauthorized product delete attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", "error",
                    "message", "You don't have permission to delete this product"
            ));
        } catch (Exception e) {
            log.error("Error deleting product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to delete product"
            ));
        }
    }
}