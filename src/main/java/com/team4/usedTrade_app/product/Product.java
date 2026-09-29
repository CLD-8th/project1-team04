package com.team4.usedTrade_app.product;

import com.team4.usedTrade_app.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.*;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 255)
    private String imagePath;

    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "CHAR(20)")
    private ProductStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Product(User seller, String title, String content, String category, String imagePath, int price) {
        this.seller = Objects.requireNonNull(seller, "seller");
        this.title = Objects.requireNonNull(title, "title");
        this.content = Objects.requireNonNull(content, "content");
        this.category = Objects.requireNonNull(category, "category");
        this.imagePath = Objects.requireNonNull(imagePath, "imagePath");
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
