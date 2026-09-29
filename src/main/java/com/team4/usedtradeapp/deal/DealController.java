package com.team4.usedtradeapp.deal;

import com.team4.usedtradeapp.common.UnauthorizedException;
import com.team4.usedtradeapp.deal.dto.DealResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DealController {
    public static final String USER_ID_SESSION_KEY = "userId";

    private final DealService dealService;

    @PostMapping("/products/{productId}/application")
    public ResponseEntity<DealResponse> apply(@PathVariable Integer productId,
            @SessionAttribute(name = USER_ID_SESSION_KEY, required = false) Integer userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.apply(productId, requireUserId(userId)));
    }

    @PostMapping("/deals/{dealId}/approve")
    public DealResponse approve(@PathVariable Integer dealId,
            @SessionAttribute(name = USER_ID_SESSION_KEY, required = false) Integer userId) {
        return dealService.approve(dealId, requireUserId(userId));
    }

    private Integer requireUserId(Integer userId) {
        if (userId == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return userId;
    }
}
