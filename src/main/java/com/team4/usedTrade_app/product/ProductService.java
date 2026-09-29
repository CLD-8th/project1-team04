package com.team4.usedTrade_app.product;

import com.team4.usedTrade_app.common.BadRequestException;
import com.team4.usedTrade_app.common.NotFoundException;
import com.team4.usedTrade_app.product.dto.ProductDetailResponse;
import com.team4.usedTrade_app.product.dto.ProductRegisterRequest;
import com.team4.usedTrade_app.product.dto.ProductResponse;
import com.team4.usedTrade_app.user.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final RecentProductService recentProductService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(Integer productId, Integer userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));

        if (userId != null) {
            try {
                recentProductService.addRecentProduct(userId, productId);
            } catch (DataAccessException exception) {
                log.warn("최근 본 상품 기록에 실패했습니다: userId={}, productId={}", userId, productId, exception);
            }
        }
        return ProductDetailResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDetailResponse> getRecentProducts(Integer userId) {
        List<Integer> productIds = recentProductService.getRecentProductIds(userId);
        if (productIds.isEmpty()) {
            return List.of();
        }
        Map<Integer, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        return productIds.stream()
                .map(products::get)
                .filter(product -> product != null)
                .map(ProductDetailResponse::from)
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
    public List<ProductResponse> getProducts(String category, ProductStatus status) {
        String categoryFilter = category == null || category.isBlank() ? null : category.trim();
        return productRepository.findAll().stream()
                .filter(product -> categoryFilter == null || product.getCategory().equals(categoryFilter))
                .filter(product -> status == null || product.getStatus() == status)
                .map(ProductResponse::from)
                .toList();
    }
}
