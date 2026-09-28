package com.team4.usedTrade_app.product.dto;

import com.team4.usedTrade_app.product.Product;
import com.team4.usedTrade_app.product.ProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductResponse {
    private Integer id;
    private Integer sellerId;
    private String title;
    private String content;
    private String category;
    private String imagePath;
    private Integer price;
    private ProductStatus status;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSeller().getId())
                .title(product.getTitle())
                .content(product.getContent())
                .category(product.getCategory())
                .imagePath(product.getImagePath())
                .price(product.getPrice())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
