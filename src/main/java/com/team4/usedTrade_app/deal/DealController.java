package com.team4.usedTrade_app.deal;

import com.team4.usedTrade_app.common.UnauthorizedException;
import com.team4.usedTrade_app.deal.dto.DealResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DealController {
    private final DealService dealService;

    @PostMapping("/products/{productId}/application")
    public ResponseEntity<DealResponse> apply(@PathVariable Integer productId,
            @AuthenticationPrincipal Integer userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.apply(productId, requireUserId(userId)));
    }

    @PostMapping("/deals/{dealId}/approve")
    public DealResponse approve(@PathVariable Integer dealId,
            @AuthenticationPrincipal Integer userId) {
        return dealService.approve(dealId, requireUserId(userId));
    }

    // FR-06 신청 목록 조회
    @GetMapping("/products/{productId}/deals")
    public List<DealResponse> getDeals(@PathVariable Integer productId,
                                       @AuthenticationPrincipal Integer userId) {
        return dealService.getDeals(productId, requireUserId(userId));
    }

    private Integer requireUserId(Integer userId) {
        if (userId == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return userId;
    }
}
