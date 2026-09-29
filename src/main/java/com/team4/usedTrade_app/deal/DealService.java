package com.team4.usedTrade_app.deal;

import com.team4.usedTrade_app.deal.dto.DealResponse;
import com.team4.usedTrade_app.product.Product;
import com.team4.usedTrade_app.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final ProductRepository productRepository;

    // FR-06 신청 목록 조회 (판매자만)
    @Transactional(readOnly = true)
    public List<DealResponse> getDeals(Integer productId, Integer loginUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (!product.getSeller().getId().equals(loginUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "판매자만 신청 목록을 조회할 수 있습니다.");
        }

        return dealRepository.findByProductId(productId).stream()
                .map(DealResponse::from)
                .toList();
    }
}