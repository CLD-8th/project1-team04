package com.team4.usedTrade_app.product;

import com.team4.usedTrade_app.product.dto.ProductDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RecentProductService recentProductService;

    // FR-05 게시글 상세 조회
    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        return ProductDetailResponse.from(product);
    }
    // FR-07 최근 본 상품 조회
    public List<ProductDetailResponse> getRecentProducts(Integer userId) {
        List<Integer> productIds = recentProductService.getRecentProductIds(userId);

        List<Product> products = productRepository.findAllById(productIds);

        return productIds.stream()
                .map(id -> products.stream()
                        .filter(product -> product.getId().equals(id))
                        .findFirst()
                        .map(ProductDetailResponse::from)
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }


}