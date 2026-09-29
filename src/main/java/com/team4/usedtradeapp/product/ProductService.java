package com.team4.usedtradeapp.product;

import com.team4.usedtradeapp.common.BadRequestException;
import com.team4.usedtradeapp.common.NotFoundException;
import com.team4.usedtradeapp.product.dto.*;
import com.team4.usedtradeapp.user.User;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
        return ProductDetailResponse.from(product);
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
