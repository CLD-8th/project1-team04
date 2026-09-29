package com.team4.usedtradeapp.product;

import com.team4.usedtradeapp.common.NotFoundException;
import com.team4.usedtradeapp.product.dto.ProductDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
        return ProductDetailResponse.from(product);
    }
}
