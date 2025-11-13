package com.bkplatform.controller;

import com.bkplatform.dto.CreateProductRequest;
import com.bkplatform.dto.UpdateProductRequest;
import com.bkplatform.model.Product;
import com.bkplatform.model.User;
import com.bkplatform.repository.UserRepository;
import com.bkplatform.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    // ✅ PUBLIC - Tìm kiếm sản phẩm (KHÔNG CẦN TOKEN)
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

    // ✅ PUBLIC - Xem chi tiết sản phẩm (KHÔNG CẦN TOKEN)
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ PROTECTED - Tạo sản phẩm mới (CẦN TOKEN)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateProductRequest req) {

        try {
            User owner = userRepository.findByUsername(principal.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getUsername()));

            Product product = productService.create(req, owner);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ✅ PROTECTED - Cập nhật sản phẩm (CẦN TOKEN)
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest req) {

        try {
            User owner = userRepository.findByUsername(principal.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getUsername()));

            return productService.update(id, req, owner)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ✅ PROTECTED - Xóa sản phẩm (CẦN TOKEN)
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {

        try {
            User owner = userRepository.findByUsername(principal.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getUsername()));

            boolean deleted = productService.delete(id, owner);
            if (deleted) {
                return ResponseEntity.ok("Product deleted successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}