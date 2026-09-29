package com.team4.usedtradeapp.deal;

import com.team4.usedtradeapp.common.*;
import com.team4.usedtradeapp.deal.dto.DealResponse;
import com.team4.usedtradeapp.product.*;
import com.team4.usedtradeapp.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DealService {
    private final DealRepository dealRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public DealResponse apply(Integer productId, Integer buyerId) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
        if (product.getStatus() != ProductStatus.SELLING) {
            throw new ConflictException("판매 중인 상품만 신청할 수 있습니다.");
        }
        if (product.getSeller().getId().equals(buyerId)) {
            throw new ForbiddenException("본인 상품에는 신청할 수 없습니다.");
        }
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new UnauthorizedException("로그인 사용자를 찾을 수 없습니다."));
        if (dealRepository.existsByProduct_IdAndBuyer_Id(productId, buyerId)) {
            throw new ConflictException("이미 신청한 상품입니다.");
        }

        Deal deal = dealRepository.saveAndFlush(new Deal(product, buyer));
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse approve(Integer dealId, Integer sellerId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new NotFoundException("거래 신청을 찾을 수 없습니다."));
        Product product = productRepository.findByIdForUpdate(deal.getProduct().getId())
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ForbiddenException("판매자만 거래 신청을 수락할 수 있습니다.");
        }
        if (product.getStatus() != ProductStatus.SELLING || deal.getStatus() != DealStatus.REQUESTED) {
            throw new ConflictException("이미 종료된 거래 신청입니다.");
        }

        deal.approve();
        product.markSold();
        return DealResponse.from(deal);
    }
}
