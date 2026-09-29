package com.team4.usedtradeapp.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.usedtradeapp.deal.DealRepository;
import com.team4.usedtradeapp.user.User;
import com.team4.usedtradeapp.user.UserRepository;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {
    private static final byte[] PNG_IMAGE = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/D8sAAAAASUVORK5CYII=");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User seller;

    @BeforeEach
    void setUp() {
        dealRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        seller = userRepository.save(BeanUtils.instantiateClass(User.class));
    }

    @Test
    void registerProductAndServeImage() throws Exception {
        String response = mockMvc.perform(multipart("/api/products")
                        .file(new MockMultipartFile("image", "book.png", "image/png", PNG_IMAGE))
                        .param("title", "중고 책")
                        .param("content", "설명")
                        .param("category", "도서")
                        .param("price", "10000")
                        .sessionAttr("userId", seller.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sellerId").value(seller.getId()))
                .andExpect(jsonPath("$.status").value("SELLING"))
                .andReturn().getResponse().getContentAsString();

        String imagePath = new ObjectMapper().readTree(response).get("imagePath").asText();
        mockMvc.perform(get(imagePath))
                .andExpect(status().isOk())
                .andExpect(content().bytes(PNG_IMAGE));
    }

    @Test
    void getProducts() throws Exception {
        Product product = productRepository.save(new Product(
                seller, "중고 책", "설명", "도서", "/uploads/book.png", 10000));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(product.getId()))
                .andExpect(jsonPath("$[0].sellerId").value(seller.getId()))
                .andExpect(jsonPath("$[0].status").value("SELLING"));
    }

    @Test
    void registrationRequiresSessionAndImage() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "book.png", "image/png", PNG_IMAGE);
        mockMvc.perform(multipart("/api/products")
                        .file(image)
                        .param("title", "중고 책")
                        .param("content", "설명")
                        .param("category", "도서")
                        .param("price", "10000"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(multipart("/api/products")
                        .param("title", "중고 책")
                        .param("content", "설명")
                        .param("category", "도서")
                        .param("price", "10000")
                        .sessionAttr("userId", seller.getId()))
                .andExpect(status().isBadRequest());
    }
}
