package com.team4.usedTrade_app.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.usedTrade_app.product.dto.ProductRegisterRequest;
import com.team4.usedTrade_app.user.User;
import com.team4.usedTrade_app.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ProductControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ProductController productController;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setCustomArgumentResolvers(new com.team4.usedTrade_app.auth.LoginUserArgumentResolver(userRepository))
                .build();
    }

    @Test
    void registerProduct() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/products")
                .file("image", "dummy image content".getBytes())
                .param("title", "Test Title")
                .param("content", "Test Content")
                .param("category", "Electronics")
                .param("price", "10000")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.sellerId").exists())
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.price").value(10000))
                .andExpect(jsonPath("$.imagePath").exists())
                .andExpect(jsonPath("$.status").value("SELLING"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getProducts() throws Exception {
        // Create a product first to ensure the list is not empty
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/products")
                .file("image", "dummy image content".getBytes())
                .param("title", "Test Title")
                .param("content", "Test Content")
                .param("category", "Electronics")
                .param("price", "10000")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].sellerId").exists())
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].content").exists())
                .andExpect(jsonPath("$[0].category").exists())
                .andExpect(jsonPath("$[0].price").exists())
                .andExpect(jsonPath("$[0].status").exists())
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

}
