package com.example.productapi.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PatchProductRequest(

        @Size(min = 1, max = 64, message = "SKU must be between 1 and 64 characters")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "SKU may contain only letters, numbers, hyphens and underscores")
        String sku,

        @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
        String name,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @Size(min = 1, max = 100, message = "Category must be between 1 and 100 characters")
        String category,

        @DecimalMin(value = "0.00", message = "Price must be zero or greater")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimals")
        BigDecimal price,

        @PositiveOrZero(message = "Stock must be zero or greater")
        Integer stock,

        Boolean active
) {
}
