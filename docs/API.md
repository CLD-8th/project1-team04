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

### FR-03·FR-04 연동 계약

- 두 요청은 본문 없이 호출하며, 로그인 기능이 세션에 `Integer` 타입 `userId`를 저장해야 한다. 세션에 이 값이 없으면 `401`을 반환한다. 이 브랜치는 로그인·상품 등록 API를 제공하지 않는다.
- FR-03은 판매 중인 상품에 `REQUESTED` 거래 신청을 만들고 `201`을 반환한다. 동일 구매자의 동일 상품 중복 신청은 `409`, 판매자 본인의 신청은 `403`이다.
- FR-04는 상품 판매자만 호출할 수 있다. 선택한 거래를 `APPROVED`, 상품을 `SOLD`로 변경하고 `200`을 반환한다. 다른 신청은 `REQUESTED`로 남지만 더는 수락할 수 없다.
- 두 응답은 `dealId`, `productId`, `buyerId`, `status`, `productStatus`를 포함한다.
- 업무 오류는 `status`, `code`, `message`, `timestamp`를 담은 JSON으로 반환한다. 없는 대상은 `404`, 권한 부족은 `403`, 중복 신청이나 종료된 거래는 `409`다.
