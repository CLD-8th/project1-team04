
  | 항목 | 설계 |
  | --- | --- |
  | 목적 | 사용자별 최근 본 상품 최대 20개 |
  | 키 | recent:products:{UserId} |
  | 자료구조 | Redis List |
  | 값 | 상품 ID |
  | TTL | 마지막 조회로부터 7일 |

### 갱신

- 로그인한 사용자가  `GET /api/products/{productsId}` 를 호출하면 맨 앞에 추가한 뒤 20개로 자르고 TTL을 갱신
- 동일한 상품이 캐싱되어 있었다면 기존 값을 제거

### 조회

- `GET /api/products/recent` 에서 상품 ID 목록을 읽고 DB에서 상품 정보를 조회
- 결과는 Redis에 저장된 최근 순서로 반환
- 판매완료 상품도 포함해서 반환

### 데이터

- Redis에는 상품 상세정보 없이 상품 ID만 저장
- Redis 장애 시 DB에서 상품 상세 정보 조회 가능