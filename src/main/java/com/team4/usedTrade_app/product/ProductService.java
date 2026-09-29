package com.team4.usedTrade_app.product;

import com.team4.usedTrade_app.common.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import com.team4.usedTrade_app.product.dto.ProductDetailResponse;
import com.team4.usedTrade_app.product.dto.ProductRegisterRequest;
import com.team4.usedTrade_app.product.dto.ProductResponse;
import com.team4.usedTrade_app.user.User;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.Objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RecentProductService recentProductService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(Integer productId, Integer userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "상품을 찾을 수 없습니다."
                ));

        recentProductService.addRecentProduct(userId, productId);

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



    @Transactional
    public ProductResponse registerProduct(User seller, ProductRegisterRequest request) {
        MultipartFile image = request.getImage();
        if (image == null || image.isEmpty()) {
            throw new BadRequestException("상품 사진은 필수입니다.");
        }

        String extension;
        if ("image/png".equals(image.getContentType())) {
            extension = ".png";
        } else if ("image/jpeg".equals(image.getContentType())) {
            extension = ".jpg";
        } else {
            throw new BadRequestException("PNG 또는 JPEG 사진만 등록할 수 있습니다.");
        }

        String filename = UUID.randomUUID() + extension;
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            image.transferTo(uploadPath.resolve(filename).toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("상품 사진을 저장할 수 없습니다.", exception);
        }

        Product product = new Product(seller, request.getTitle(), request.getContent(),
                request.getCategory(), "/uploads/" + filename, request.getPrice());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }
}
