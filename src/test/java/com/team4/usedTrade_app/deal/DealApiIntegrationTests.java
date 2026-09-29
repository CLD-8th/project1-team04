package com.team4.usedTrade_app.deal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team4.usedTrade_app.auth.TokenProvider;
import com.team4.usedTrade_app.auth.TokenRevocationService;
import com.team4.usedTrade_app.product.Product;
import com.team4.usedTrade_app.product.ProductRepository;
import com.team4.usedTrade_app.product.ProductStatus;
import com.team4.usedTrade_app.user.User;
import com.team4.usedTrade_app.user.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DealApiIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @MockitoBean
    private TokenRevocationService tokenRevocationService;

    @BeforeEach
    void clearData() {
        dealRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User newUser() {
        return User.builder()
                .email(UUID.randomUUID() + "@example.test")
                .password("test-password")
                .nickname("tester")
                .build();
    }

    private String bearer(User user) {
        return "Bearer " + tokenProvider.issueAccessToken(user.getId());
    }

    @Test
    void multipleBuyersCanApplyButOnlyOneDealCanBeApproved() throws Exception {
        User seller = userRepository.save(newUser());
        User firstBuyer = userRepository.save(newUser());
        User secondBuyer = userRepository.save(newUser());
        User thirdBuyer = userRepository.save(newUser());
        Product product = productRepository.save(new Product(
                seller, "중고 책", "설명", "도서", "book.jpg", 10000));

        mockMvc.perform(post("/api/products/{productId}/application", product.getId())
                        .header("Authorization", bearer(firstBuyer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(product.getId()))
                .andExpect(jsonPath("$.buyerId").value(firstBuyer.getId()))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.productStatus").value("SELLING"));
        mockMvc.perform(post("/api/products/{productId}/application", product.getId())
                        .header("Authorization", bearer(secondBuyer)))
                .andExpect(status().isCreated());

        assertEquals(2, dealRepository.count());
        Deal approvedDeal = dealRepository.findAll().stream()
                .filter(deal -> deal.getBuyer().getId().equals(firstBuyer.getId()))
                .findFirst().orElseThrow();
        Deal remainingDeal = dealRepository.findAll().stream()
                .filter(deal -> deal.getBuyer().getId().equals(secondBuyer.getId()))
                .findFirst().orElseThrow();

        mockMvc.perform(post("/api/deals/{dealId}/approve", approvedDeal.getId())
                        .header("Authorization", bearer(seller)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.productStatus").value("SOLD"));

        assertEquals(ProductStatus.SOLD, productRepository.findById(product.getId()).orElseThrow().getStatus());
        assertEquals(DealStatus.APPROVED, dealRepository.findById(approvedDeal.getId()).orElseThrow().getStatus());
        assertEquals(DealStatus.REQUESTED, dealRepository.findById(remainingDeal.getId()).orElseThrow().getStatus());
        mockMvc.perform(post("/api/deals/{dealId}/approve", remainingDeal.getId())
                        .header("Authorization", bearer(seller)))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/products/{productId}/application", product.getId())
                        .header("Authorization", bearer(thirdBuyer)))
                .andExpect(status().isConflict());
    }

    @Test
    void duplicateAndSellerApplicationsAreRejected() throws Exception {
        User seller = userRepository.save(newUser());
        User buyer = userRepository.save(newUser());
        Product product = productRepository.save(new Product(
                seller, "중고 책", "설명", "도서", "book.jpg", 10000));

        mockMvc.perform(post("/api/products/{productId}/application", product.getId())
                        .header("Authorization", bearer(seller)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/products/{productId}/application", product.getId())
                        .header("Authorization", bearer(buyer)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/products/{productId}/application", product.getId())
                        .header("Authorization", bearer(buyer)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
        assertEquals(1, dealRepository.count());
    }

    @Test
    void authenticationAndSellerAreRequired() throws Exception {
        User seller = userRepository.save(newUser());
        User buyer = userRepository.save(newUser());
        Product product = productRepository.save(new Product(
                seller, "중고 책", "설명", "도서", "book.jpg", 10000));

        mockMvc.perform(post("/api/products/{productId}/application", product.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/products/{productId}/application", 999999)
                        .header("Authorization", bearer(buyer)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mockMvc.perform(post("/api/products/{productId}/application", product.getId())
                        .header("Authorization", bearer(buyer)))
                .andExpect(status().isCreated());
        Deal deal = dealRepository.findAll().getFirst();

        mockMvc.perform(post("/api/deals/{dealId}/approve", deal.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/deals/{dealId}/approve", deal.getId())
                        .header("Authorization", bearer(buyer)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mockMvc.perform(post("/api/deals/{dealId}/approve", 999999)
                        .header("Authorization", bearer(seller)))
                .andExpect(status().isNotFound());
    }

    @Test
    void productDetailUsesSharedNotFoundResponse() throws Exception {
        mockMvc.perform(get("/api/products/{productId}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void databaseEnforcesOneApplicationPerBuyerAndProduct() {
        User seller = userRepository.save(newUser());
        User buyer = userRepository.save(newUser());
        Product product = productRepository.save(new Product(
                seller, "중고 책", "설명", "도서", "book.jpg", 10000));

        dealRepository.saveAndFlush(new Deal(product, buyer));

        assertThrows(DataIntegrityViolationException.class,
                () -> dealRepository.saveAndFlush(new Deal(product, buyer)));
    }
}
