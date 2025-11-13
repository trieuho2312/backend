package com.bkplatform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateProductRequest {
    private String name;
    private BigDecimal price;
    private String description;
    private Integer stockQuantity;
    private Long categoryId;
}
