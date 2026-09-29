package com.team4.usedTrade_app.deal.dto;

import com.team4.usedTrade_app.deal.Deal;
import com.team4.usedTrade_app.deal.DealStatus;
import com.team4.usedTrade_app.product.ProductStatus;

public record DealResponse(
        Integer dealId,
        Integer productId,
        Integer buyerId,
        DealStatus status,
        ProductStatus productStatus) {

    public static DealResponse from(Deal deal) {
        return new DealResponse(
                deal.getId(),
                deal.getProduct().getId(),
                deal.getBuyer().getId(),
                deal.getStatus(),
                deal.getProduct().getStatus());
    }
}
