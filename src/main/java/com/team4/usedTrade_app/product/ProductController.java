package com.team4.usedTrade_app.product;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ProductDetailResponse detail(
            @PathVariable Integer productId,
            @AuthenticationPrincipal Integer userId) {
        // TODO: 로그인 사용자면 최근 본 상품 기록
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