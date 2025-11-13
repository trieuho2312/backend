package com.bkplatform.service;

import com.bkplatform.dto.CreateProductRequest;
import com.bkplatform.dto.UpdateProductRequest;
import com.bkplatform.exception.ResourceNotFoundException;
import com.bkplatform.exception.UnauthorizedException;
import com.bkplatform.model.*;
import com.bkplatform.repository.CategoryRepository;
import com.bkplatform.repository.ProductRepository;
import com.bkplatform.repository.ShopRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Search products with filters, sorting, and pagination
     */
    public Page<Product> search(String search, Long categoryId, String sortBy, int page, int size) {
        Sort sort = buildSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by name or description
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase().trim() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(namePredicate, descPredicate));
            }

            // Filter by category
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable);
    }

    /**
     * Build Sort object from sort parameter
     */
    private Sort buildSort(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (sortBy.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "name");
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "name");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    /**
     * Find product by ID
     */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Get product by ID (throws exception if not found)
     */
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    /**
     * Create new product
     */
    @Transactional
    public Product create(CreateProductRequest req, User owner) {
        log.info("Creating product for user: {}", owner.getUsername());

        // ✅ Validate price
        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        // ✅ Validate name
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        // ✅ Validate stock quantity
        if (req.getStockQuantity() != null && req.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        // Get or create shop for owner
        Shop shop = shopRepository.findByOwner(owner).orElseGet(() -> {
            log.info("Creating new shop for user: {}", owner.getUsername());
            Shop newShop = Shop.builder()
                    .owner(owner)
                    .name(owner.getFullName() + "'s Shop")
                    .build();
            return shopRepository.save(newShop);
        });

        // Build product
        Product product = Product.builder()
                .shop(shop)
                .name(req.getName().trim())
                .price(req.getPrice())
                .description(req.getDescription() != null ? req.getDescription().trim() : null)
                .stockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0)
                .build();

        // ✅ Validate and set category
        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + req.getCategoryId()
                    ));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        log.info("Created product with id: {}", saved.getProductId());

        return saved;
    }

    /**
     * Update product (only by owner)
     * ✅ FIX: Return Optional<Product> to match controller expectation
     */
    @Transactional
    public Optional<Product> update(Long id, UpdateProductRequest req, User owner) {
        log.info("Updating product {} by user: {}", id, owner.getUsername());

        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            return Optional.empty();
        }

        Product product = productOpt.get();

        // ✅ Check ownership
        if (!product.getShop().getOwner().getUserId().equals(owner.getUserId())) {
            throw new UnauthorizedException("You don't have permission to update this product");
        }

        // ✅ Update fields with validation
        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            product.setName(req.getName().trim());
        }

        if (req.getPrice() != null) {
            if (req.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Price must be greater than 0");
            }
            product.setPrice(req.getPrice());
        }

        if (req.getDescription() != null) {
            product.setDescription(req.getDescription().trim());
        }

        if (req.getStockQuantity() != null) {
            if (req.getStockQuantity() < 0) {
                throw new IllegalArgumentException("Stock quantity cannot be negative");
            }
            product.setStockQuantity(req.getStockQuantity());
        }

        // ✅ Validate and update category
        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + req.getCategoryId()
                    ));
            product.setCategory(category);
        }

        Product updated = productRepository.save(product);
        log.info("Updated product {}", id);

        return Optional.of(updated);
    }

    /**
     * Delete product (only by owner)
     * ✅ FIX: Return boolean to match controller expectation
     */
    @Transactional
    public boolean delete(Long id, User owner) {
        log.info("Deleting product {} by user: {}", id, owner.getUsername());

        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            return false;
        }

        Product product = productOpt.get();

        // ✅ Check ownership
        if (!product.getShop().getOwner().getUserId().equals(owner.getUserId())) {
            throw new UnauthorizedException("You don't have permission to delete this product");
        }

        productRepository.delete(product);
        log.info("Deleted product {}", id);

        return true;
    }

    /**
     * Get products by shop
     */
    public Page<Product> getProductsByShop(Long shopId, int page, int size) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Product> spec = (root, query, cb) ->
                cb.equal(root.get("shop"), shop);

        return productRepository.findAll(spec, pageable);
    }

    /**
     * Update stock quantity (for inventory management)
     */
    @Transactional
    public Product updateStock(Long productId, int quantity, User owner) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check ownership
        if (!product.getShop().getOwner().getUserId().equals(owner.getUserId())) {
            throw new UnauthorizedException("You don't have permission to update stock");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        product.setStockQuantity(quantity);
        return productRepository.save(product);
    }
}