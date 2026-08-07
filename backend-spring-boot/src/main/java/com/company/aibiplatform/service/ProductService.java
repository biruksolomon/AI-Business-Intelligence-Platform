package com.company.aibiplatform.service;

import com.company.aibiplatform.dto.product.ProductRequest;
import com.company.aibiplatform.dto.product.ProductResponse;
import com.company.aibiplatform.entity.Category;
import com.company.aibiplatform.entity.Product;
import com.company.aibiplatform.exception.ResourceNotFoundException;
import com.company.aibiplatform.repository.CategoryRepository;
import com.company.aibiplatform.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("A product with SKU '" + request.getSku() + "' already exists");
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .category(category)
                .price(request.getPrice())
                .costPrice(request.getCostPrice())
                .stockQuantity(request.getStockQuantity())
                .lowStockThreshold(request.getLowStockThreshold())
                .build();

        return toResponse(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setCategory(category);
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setLowStockThreshold(request.getLowStockThreshold());

        return toResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = getOrThrow(id);
        productRepository.delete(product);
    }

    public ProductResponse getById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> getLowStock() {
        return productRepository.findLowStockProducts().stream().map(this::toResponse).toList();
    }

    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .price(p.getPrice())
                .costPrice(p.getCostPrice())
                .stockQuantity(p.getStockQuantity())
                .lowStockThreshold(p.getLowStockThreshold())
                .lowStock(p.getStockQuantity() <= p.getLowStockThreshold())
                .build();
    }
}