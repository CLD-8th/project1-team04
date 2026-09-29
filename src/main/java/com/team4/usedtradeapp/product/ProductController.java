package com.team4.usedtradeapp.product;

import com.team4.usedtradeapp.product.dto.ProductDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ProductDetailResponse getDetail(@PathVariable Integer productId) {
        // TODO: 로그인 사용자의 최근 본 상품 기록 연동
        return productService.getDetail(productId);
    }
}
