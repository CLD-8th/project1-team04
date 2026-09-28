package com.team4.usedTrade_app.product;

import com.team4.usedTrade_app.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 100)
    private String category;

    @Column(length = 255)
    private String imagePath;

    private int price;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "CHAR(20)")
    private ProductStatus status;

    private LocalDateTime createdAt;

    @Builder
    public Product(User seller, String title, String content, String category, String imagePath, int price) {
        this.seller = seller;
        this.title = title;
        this.content = content;
        this.category = category;
        this.imagePath = imagePath;
        this.price = price;
        this.status = ProductStatus.SELLING;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void markSold() {
        this.status = ProductStatus.SOLD;
    }
}