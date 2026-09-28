package com.team4.usedTrade_app.product.dto;

import com.team4.usedTrade_app.product.Product;
import com.team4.usedTrade_app.product.ProductStatus;

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

    public static ProductDetailResponse from(Product p) {
        return new ProductDetailResponse(
                p.getId(),
                p.getTitle(),
                p.getContent(),
                p.getCategory(),
                p.getImagePath(),
                p.getPrice(),
                p.getStatus(),
                p.getSeller().getId(),
                p.getCreatedAt()
        );
    }
}