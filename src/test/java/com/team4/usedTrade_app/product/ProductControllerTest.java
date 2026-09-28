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

import java.lang.reflect.Field;

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
        // Initialize mock user, since data.sql might not run before tests in the same way or might be rolled back
        if (userRepository.count() == 0) {
            User user = User.builder().build();
            userRepository.save(user);
        }
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
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.status").value("SELLING"));
    }

    @Test
    void getProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    private void setField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
