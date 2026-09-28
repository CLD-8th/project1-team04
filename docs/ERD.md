```mermaid
erDiagram
	user {
		id int PK
		%% 스켈레톤 재활용, 상세 필드 생략
	}
	
	product {
		id int PK
		seller_id int FK
		title VARCHAR(200)
		content TEXT
		category VARCHAR(100)

		image_path VARCHAR(255)
		%% 로컬 파일 시스템 이미지 경로; 현재 s3 사용하지 않으므로 단순하게 다룸
		
		price int
		status CHAR(20)
		created_at DATETIME "DEFAULT CURRENT_TIMESTAMP"
	}
	
	deal {
		id int PK
		product_id int FK
		%% 셀러 id는 product 통해서 접근
		buyer_id int FK
		status CHAR(20)
	}
	
	user ||--o{ deal : "상품 등록 | 거래 요청"
	product ||--|{ deal : "has"
```

### product.status 상태 표

| 값 | 설명 | 비고 |
| --- | --- | --- |
| SELLING | 등록 직후 초기 상태 (판매 중) |  |
| RESERVED | 거래가 예약됨 |  |
| SOLD | 거래 요청을 승인하여 판매가 완료 |  |
|  |  | 현재 거래 취소는 기능 명세에 없음 |

### deal.status 상태 표

| 값 | 설명 | 비고 |
| --- | --- | --- |
| REQUESTED | 거래 요청됨 |  |
| APPROVED | 거래 수락됨 |  |

### 제약 조건

- 한 사용자는 하나의 상품에 동시에 하나의 거래 요청만 할 수 있음 (UK)