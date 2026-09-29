package com.team4.usedTrade_app.product;

import com.team4.usedTrade_app.auth.LoginUser;
import com.team4.usedTrade_app.product.dto.*;
import com.team4.usedTrade_app.user.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
