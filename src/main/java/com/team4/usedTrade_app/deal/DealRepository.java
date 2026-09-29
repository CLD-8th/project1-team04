package com.team4.usedTrade_app.deal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, Integer> {
    boolean existsByProduct_IdAndBuyer_Id(Integer productId, Integer buyerId);
}
