package com.team4.usedTrade_app.product;

import com.team4.usedTrade_app.auth.LoginUser;
import com.team4.usedTrade_app.product.dto.ProductDetailResponse;
import com.team4.usedTrade_app.product.dto.ProductRegisterRequest;
import com.team4.usedTrade_app.product.dto.ProductResponse;
import com.team4.usedTrade_app.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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