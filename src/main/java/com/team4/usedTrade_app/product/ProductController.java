package com.team4.usedTrade_app.product;

import com.team4.usedTrade_app.auth.LoginUser;
import com.team4.usedTrade_app.product.dto.*;
import com.team4.usedTrade_app.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ProductDetailResponse detail(
            @PathVariable Integer productId,
            @AuthenticationPrincipal Integer userId) {
        return productService.getDetail(productId, userId);
    }

    // FR-07 최근 본 상품 조회
    @GetMapping("/recent")
    public List<ProductDetailResponse> recentProducts(
            @AuthenticationPrincipal Integer userId) {
        return productService.getRecentProducts(userId);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductResponse> registerProduct(
            @LoginUser User seller, @Valid @ModelAttribute ProductRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.registerProduct(seller, request));
    }

    @GetMapping
    public List<ProductResponse> getProducts() {
        return productService.getProducts();
    }
}
