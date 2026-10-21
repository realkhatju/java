package com.example.productapi.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        String category,
        BigDecimal price,
        int stock,
        boolean active,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
