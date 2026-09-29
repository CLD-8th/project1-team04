package com.team4.usedTrade_app.product;
import com.team4.usedTrade_app.auth.LoginUser;
import com.team4.usedTrade_app.product.dto.ProductRegisterRequest;
import com.team4.usedTrade_app.product.dto.ProductResponse;
import com.team4.usedTrade_app.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.team4.usedTrade_app.product.dto.ProductDetailResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // FR-05 게시글 상세 조회
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

    // FR-01 게시글 등록
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ProductResponse> registerProduct(
            @LoginUser User user,
            @Valid @ModelAttribute ProductRegisterRequest request) {
        ProductResponse response = productService.registerProduct(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // FR-02 게시글 목록
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts() {
        List<ProductResponse> responses = productService.getProducts();
        return ResponseEntity.ok(responses);
    }
}