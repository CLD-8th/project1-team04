#### URL/API 목록 — 중고거래 게시판

| # | 기능 | Method | URL | 담당 | 인증 | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| FR-01 | 중고거래 게시글 등록 | POST | /api/products | 이유찬 | 필요 | 제목·설명·가격·카테고리·사진 1장 등록 |
| FR-02 | 게시글 목록 | GET | /api/products | 이유찬 | 불필요 | 카테고리·판매 상태로 조회 |
| FR-03 | 거래 신청 | POST | /api/products/{productId}/application | 진호용 | 필요 | 구매자가 해당 상품에 거래 신청 |
| FR-04 | 거래 수락 | POST | /api/deals/{dealId}/approve | 진호용 | 필요 | 판매자가 거래 신청 수락 |
| FR-05 | 게시글 상세 조회 | GET | /api/products/{productId} | 황경민 | 불필요 | 특정 상품 상세 조회 |
| FR-06 | 신청 목록 조회 | GET | /api/products/{productId}/deals | 황경민 | 필요 | 판매자가 신청 목록 조회 |
| FR-07 | 최근 본 상품 조회 | GET | /api/products/recent | 방지은 | 필요 | Redis 활용 기능 |
| FR-08 | 로그인 | POST | /api/users/login | 김도현 | 불필요 | 스켈레톤 재활용 |
| FR-09 | 로그아웃 | POST | /api/users/logout | 방지은 | 필요 | 스켈레톤 재활용 |
| FR-10 | 회원가입 | POST | /api/users | 김도현 | 불필요 | 스켈레톤 재활용 |