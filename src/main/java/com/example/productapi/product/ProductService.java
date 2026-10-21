package com.example.productapi.product;

import com.example.productapi.common.PageResponse;
import com.example.productapi.exception.DuplicateResourceException;
import com.example.productapi.exception.ResourceNotFoundException;
import com.example.productapi.product.dto.CreateProductRequest;
import com.example.productapi.product.dto.PatchProductRequest;
import com.example.productapi.product.dto.ProductResponse;
import com.example.productapi.product.dto.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public PageResponse<ProductResponse> findAll(String q, String category, Boolean active,
                                                 BigDecimal minPrice, BigDecimal maxPrice,
                                                 Pageable pageable) {
        String search = StringUtils.hasText(q) ? q.trim() : null;
        String categoryFilter = StringUtils.hasText(category) ? category.trim() : null;

        Page<ProductResponse> page = productRepository
                .search(search, categoryFilter, active, minPrice, maxPrice, pageable)
                .map(ProductMapper::toResponse);

        return PageResponse.from(page);
    }

    public ProductResponse findById(Long id) {
        return ProductMapper.toResponse(getProduct(id));
    }

    public ProductResponse findBySku(String sku) {
        return productRepository.findBySkuIgnoreCase(sku.trim())
                .map(ProductMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product with SKU '%s' was not found".formatted(sku)));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        String sku = request.sku().trim();
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new DuplicateResourceException("Product with SKU '%s' already exists".formatted(sku));
        }

        Product product = productRepository.saveAndFlush(ProductMapper.toEntity(request));
        return ProductMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = getProduct(id);
        String sku = request.sku().trim();

        if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
            throw new DuplicateResourceException("Product with SKU '%s' already exists".formatted(sku));
        }

        ProductMapper.apply(request, product);
        return ProductMapper.toResponse(productRepository.saveAndFlush(product));
    }

    @Transactional
    public ProductResponse patch(Long id, PatchProductRequest request) {
        Product product = getProduct(id);

        if (request.sku() != null) {
            String sku = request.sku().trim();
            if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
                throw new DuplicateResourceException("Product with SKU '%s' already exists".formatted(sku));
            }
        }

        ProductMapper.apply(request, product);
        return ProductMapper.toResponse(productRepository.saveAndFlush(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);
        productRepository.delete(product);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id %d was not found".formatted(id)));
    }
}
