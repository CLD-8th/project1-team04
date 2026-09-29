package com.team4.usedtradeapp.product.dto;

import com.team4.usedtradeapp.product.Product;
import com.team4.usedtradeapp.product.ProductStatus;

import java.time.LocalDateTime;

public record ProductDetailResponse(
        Integer id,
        String title,
        String content,
        String category,
        String imagePath,
        int price,
        ProductStatus status,
        Integer sellerId,
        LocalDateTime createdAt
) {

    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getTitle(),
                product.getContent(),
                product.getCategory(),
                product.getImagePath(),
                product.getPrice(),
                product.getStatus(),
                product.getSeller().getId(),
                product.getCreatedAt()
        );
    }
}
