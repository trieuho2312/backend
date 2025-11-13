package com.bkplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    @NotBlank private String name;
    @NotNull @PositiveOrZero private BigDecimal price;
    private String description;
    @PositiveOrZero private Integer stockQuantity = 0;
    private Long categoryId;
}

