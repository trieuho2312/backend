package com.bkplatform.service;

import com.bkplatform.dto.CreateProductRequest;
import com.bkplatform.dto.UpdateProductRequest;
import com.bkplatform.model.*;
import com.bkplatform.repository.ProductRepository;
import com.bkplatform.repository.ShopRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    // ✅ Tìm kiếm sản phẩm theo từ khóa, danh mục, sắp xếp, phân trang
    public Page<Product> search(String search, Long categoryId, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable);
    }

    // ✅ Tìm sản phẩm theo ID
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    // ✅ Tạo sản phẩm mới
    @Transactional
    public Product create(CreateProductRequest req, User owner) {
        // Nếu người dùng chưa có shop thì tự động tạo
        Shop shop = shopRepository.findByOwner(owner).orElseGet(() -> {
            Shop s = Shop.builder()
                    .owner(owner)
                    .name(owner.getFullName() + "'s shop")
                    .build();
            return shopRepository.save(s);
        });

        Product product = Product.builder()
                .shop(shop)
                .name(req.getName())
                .price(req.getPrice())
                .description(req.getDescription())
                .stockQuantity(req.getStockQuantity() == null ? 0 : req.getStockQuantity())
                .build();

        if (req.getCategoryId() != null) {
            Category c = new Category();
            c.setCategoryId(req.getCategoryId());
            product.setCategory(c);
        }

        return productRepository.save(product);
    }

    // ✅ Cập nhật sản phẩm (nếu là chủ shop)
    @Transactional
    public Optional<Product> update(Long id, UpdateProductRequest req, User owner) {
        return productRepository.findById(id).map(p -> {
            if (!p.getShop().getOwner().getUserId().equals(owner.getUserId())) {
                throw new RuntimeException("Không có quyền sửa sản phẩm này");
            }

            if (req.getName() != null) p.setName(req.getName());
            if (req.getPrice() != null) p.setPrice(req.getPrice());
            if (req.getDescription() != null) p.setDescription(req.getDescription());
            if (req.getStockQuantity() != null) p.setStockQuantity(req.getStockQuantity());
            if (req.getCategoryId() != null) {
                Category c = new Category();
                c.setCategoryId(req.getCategoryId());
                p.setCategory(c);
            }

            return productRepository.save(p);
        });
    }

    // ✅ Xóa sản phẩm (nếu là chủ shop)
    @Transactional
    public boolean delete(Long id, User owner) {
        return productRepository.findById(id)
                .map(p -> {
                    if (!p.getShop().getOwner().getUserId().equals(owner.getUserId())) {
                        throw new RuntimeException("Không có quyền xóa sản phẩm này");
                    }
                    productRepository.delete(p);
                    return true;
                })
                .orElse(false);
    }
}