package com.team4.usedTrade_app.deal.dto;

import com.team4.usedTrade_app.deal.Deal;
import com.team4.usedTrade_app.deal.DealStatus;

public record DealResponse(
        Integer dealId,
        Integer buyerId,
        DealStatus status
) {

    public static DealResponse from(Deal deal) {
        return new DealResponse(
                deal.getId(),
                deal.getBuyer().getId(),
                deal.getStatus()
        );
    }
}