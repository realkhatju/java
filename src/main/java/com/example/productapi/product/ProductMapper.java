package com.example.productapi.product;

import com.example.productapi.product.dto.CreateProductRequest;
import com.example.productapi.product.dto.PatchProductRequest;
import com.example.productapi.product.dto.ProductResponse;
import com.example.productapi.product.dto.UpdateProductRequest;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static Product toEntity(CreateProductRequest request) {
        return new Product(
                request.sku().trim(),
                request.name().trim(),
                trimToNull(request.description()),
                request.category().trim(),
                request.price(),
                request.stock(),
                request.active() == null || request.active()
        );
    }

    public static void apply(UpdateProductRequest request, Product product) {
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setDescription(trimToNull(request.description()));
        product.setCategory(request.category().trim());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setActive(request.active());
    }

    public static void apply(PatchProductRequest request, Product product) {
        if (request.sku() != null) {
            product.setSku(request.sku().trim());
        }
        if (request.name() != null) {
            product.setName(request.name().trim());
        }
        if (request.description() != null) {
            product.setDescription(trimToNull(request.description()));
        }
        if (request.category() != null) {
            product.setCategory(request.category().trim());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stock() != null) {
            product.setStock(request.stock());
        }
        if (request.active() != null) {
            product.setActive(request.active());
        }
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.isActive(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
