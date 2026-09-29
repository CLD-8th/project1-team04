package com.team4.usedtradeapp.deal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, Integer> {
    boolean existsByProduct_IdAndBuyer_Id(Integer productId, Integer buyerId);
}
