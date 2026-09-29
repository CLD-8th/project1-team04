package com.team4.usedTrade_app.deal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Integer> {

    // FR-06 신청 목록 조회
    List<Deal> findByProductId(Integer productId);
}