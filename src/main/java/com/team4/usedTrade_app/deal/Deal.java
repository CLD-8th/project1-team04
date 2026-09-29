package com.team4.usedTrade_app.deal;

import com.team4.usedTrade_app.product.Product;
import com.team4.usedTrade_app.user.User;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.*;

@Entity
@Table(name = "deal", uniqueConstraints = @UniqueConstraint(
        name = "uk_deal_product_buyer", columnNames = {"product_id", "buyer_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DealStatus status;

    @Builder
    public Deal(Product product, User buyer) {
        this.product = Objects.requireNonNull(product, "product");
        this.buyer = Objects.requireNonNull(buyer, "buyer");
        this.status = DealStatus.REQUESTED;
    }

    public void approve() {
        status = DealStatus.APPROVED;
    }
}
