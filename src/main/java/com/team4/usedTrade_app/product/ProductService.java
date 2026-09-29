package com.team4.usedTrade_app.product;

import com.team4.usedTrade_app.product.dto.ProductDetailResponse;
import com.team4.usedTrade_app.product.dto.ProductRegisterRequest;
import com.team4.usedTrade_app.product.dto.ProductResponse;
import com.team4.usedTrade_app.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    // FR-05 게시글 상세 조회
    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        return ProductDetailResponse.from(product);
    }

    @Transactional
    public ProductResponse registerProduct(User seller, ProductRegisterRequest request) {
        String imagePath = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String filename = System.currentTimeMillis() + "_" + request.getImage().getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(filename).toAbsolutePath();
                request.getImage().transferTo(filePath.toFile());
                imagePath = "/uploads/" + filename;
            } catch (IOException e) {
                throw new RuntimeException("이미지 파일 저장에 실패했습니다.", e);
            }
        }

        Product product = Product.builder()
                .seller(seller)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .price(request.getPrice())
                .imagePath(imagePath)
                .build();

        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }
}