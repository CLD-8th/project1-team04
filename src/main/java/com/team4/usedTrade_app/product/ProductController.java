package com.team4.usedTrade_app.product;

import com.team4.usedTrade_app.product.dto.ProductDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // FR-05 게시글 상세 조회
    @GetMapping("/{productId}")
    public ProductDetailResponse detail(@PathVariable Integer productId) {
        // TODO: 로그인 사용자면 최근 본 상품 기록 (FR-07 방지은님 연동)
        return productService.getDetail(productId);
    }
}