# 도메인·기능 정의서 — 중고거래 게시판

## 빠른 시작 (Docker Compose)

Docker Engine과 Compose가 필요하다. 이 프로젝트는 앱 이미지를 `linux/amd64`와 `linux/arm64`로 함께 빌드하므로 Docker의 containerd 이미지 저장소가 활성화되어 있어야 한다. `docker info --format '{{json .DriverStatus}}'`에서 `io.containerd.snapshotter.v1`을 확인할 수 있다.

```bash
git clone https://github.com/CLD-8th/project1-team04.git
cd project1-team04
cp .env.example .env
# .env의 DB_PASSWORD, DB_ROOT_PASSWORD, REDIS_PASSWORD, JWT_SECRET 입력
# JWT_SECRET은 UTF-8 기준 32바이트 이상으로 설정
docker compose config -q
docker compose up -d --build --wait
docker compose ps
```

`--build`는 Dockerfile에서 JAR과 앱 이미지를 빌드한다. `--wait`는 DB·Redis와 앱이 실행 또는 정상 상태가 될 때까지 기다린다. 기본 앱 포트는 `8080`이며 `.env`의 `APP_PORT`로 변경할 수 있다.

| 확인 대상 | Docker 호스트에서 여는 URL |
| --- | --- |
| 중고거래 사이트 | http://localhost:8080/ |
| API 테스트 페이지 | http://localhost:8080/api-test.html |
| 상태 확인 | http://localhost:8080/actuator/health |

다른 컴퓨터에서 접속할 때는 `localhost`를 Docker 호스트의 IP 주소로 바꾼다. 포트를 변경했다면 URL의 `8080`도 같은 값으로 바꾼다.

## 1. 서비스 개요

- 한 줄 정의: 사용자가 중고 물품을 게시·조회하고, 구매할 수 있는 서비스
- 대상 사용자: 개인 판매자와 구매자

## 2. 핵심 기능

### 상품 등록, 목록 및 상세 조회

- 판매자가 제목·설명·가격·카테고리·사진을 입력해 상품을 등록한다.
- 구매자는 목록에서 상품을 찾고 상세 페이지에서 상품 정보와 거래 상태(판매중·판매완료)를 확인한다.

### 판매·구매

- 구매자가 등록된 상품을 구매 신청 한다.
- 판매자는 게시판 신청 목록에서 승인한다.

## 3. Redis 활용 기능

- 사용자별 최근 본 상품 목록 20개 저장

## 4. 파일 요소

- 상품 사진(png, jpeg)

## 5. 데이터 특성

- 게시물 작성자&구매자, 읽기 9: 쓰기 1

## 6. 트래픽 특성

- DAU 약 1만, Think Time 5~10초
- 퇴근 후 19시 피크 시간대
- 주말 또는 특정 중고 인기 상품 등록 시
- RPS 예측
    - 피크 시간대 최대 100RPS (피크 사용자 1,000 / Think Time 5~10초) 예상
    - 급증(인기 상품)시 2배 → 200RPS 예상
    - 인스턴스 처리량 X 기준 최대 K=200/X 대 서버 필요, 최소 1대 ~ 최대 K대

## 7. 중요도

- RTO: 30분
    - 장애가 발생해도 30분 이내로 서비스 복구
- RPO: 5분
    - 최대 5분 이내의 상품 등록 및 거래 상태 변경 데이터만 손실될 수 있도록 함

현재 Docker Compose 구성은 단일 Ubuntu 서버에서 실행하는 최소 동작 환경이다. 위 RTO·RPO는 목표이며, 백업·복구 절차가 없어 아직 검증되지 않았다.

## 8. 프로젝트 설정

| 항목 | 값 |
| --- | --- |
| 빌드 도구 | Gradle · Groovy |
| 언어 | Java |
| 프레임워크 버전 | Spring Boot 4.1.1 |
| 그룹 | `com.team4` |
| 이름 | `usedTrade-app` |
| 패키지 | `com.team4.usedTrade_app` |
| 포장 | Jar |
| 개발 도구 버전 | 21 |
| 최초 선택 의존성 | Spring Web |
| 추가 의존성 | Spring Data JPA, Spring Data Redis, Spring Session Data Redis, Actuator, Validation, Spring Security Crypto, Lombok, MySQL Driver |

## 9. 현재 구현 및 실행

Spring Boot 앱과 문서의 API 10개, MySQL·Redis 실행 기반이 있다. 로그인한 사용자의 상세 조회는 Redis에 최근 본 상품을 최대 20개까지 7일간 기록한다. 로그아웃은 현재 접근 토큰을 Redis에서 만료 시점까지 무효화한다.

정적 화면은 중고거래 사이트(`/`)와 API 테스트(`/api-test.html`) 두 페이지다. 판매자·구매자 로그인은 두 화면에서 공유하며, 브라우저에 저장된 접근 토큰이 만료되면 다시 로그인해야 한다. 두 화면에서 최근 본 상품 조회와 서버 로그아웃도 사용할 수 있다.

앱 이미지는 `linux/amd64`와 `linux/arm64`를 함께 빌드한다. 로컬에 다중 플랫폼 이미지를 저장할 수 있도록 Docker의 containerd 이미지 저장소를 사용한다. `docker info --format '{{json .DriverStatus}}'`에서 `io.containerd.snapshotter.v1`을 확인할 수 있다. JAR 빌드 단계는 빌드 호스트의 아키텍처에서 실행하고, 실행용 JRE 이미지는 두 아키텍처로 만든다. `--builder default`는 빌드 결과를 로컬 이미지 저장소에 넣기 위해 사용한다.

각 호스트에서 Docker Engine과 Compose를 준비하고, `.env`에 DB·Redis 비밀번호와 `JWT_SECRET`을 설정한 뒤 실행한다.

```bash
cp -n .env.example .env  # .env가 없을 때만 생성
# .env에 DB_PASSWORD, DB_ROOT_PASSWORD, REDIS_PASSWORD, JWT_SECRET 입력
docker compose config -q
./gradlew clean test bootJar --no-daemon
docker compose build --builder default app
docker image ls --tree project1-team04-app:multiarch
docker compose pull db cache
docker compose up -d --no-build --pull never --wait
docker compose ps
curl -fsS http://localhost:8080/actuator/health
python3 scripts/e2e_api.py
```

E2E 스크립트는 10개 API의 정상·경계 사례를 검증하기 위해 테스트 계정 3개와 작은 이미지가 포함된 상품 21개를 새로 만든다. 기존 데이터는 삭제하지 않으며 테스트 데이터는 DB와 업로드 볼륨에 남는다.

기본 앱 이미지 태그는 `project1-team04-app:multiarch`이며 `APP_IMAGE` 환경변수로 바꿀 수 있다. 두 호스트에서 **동일하게 빌드된 이미지**를 사용하려면 한 호스트에서 `docker compose build --builder default app`을 실행하고, 다음과 같이 두 플랫폼이 포함된 이미지를 다른 호스트로 옮긴다. 각 호스트의 `.env`는 별도로 유지한다.

```bash
docker image save project1-team04-app:multiarch | gzip > usedtrade-multiarch.tar.gz
# 다른 호스트로 usedtrade-multiarch.tar.gz를 전송한 뒤
gunzip -c usedtrade-multiarch.tar.gz | docker image load
docker compose up -d --no-build --pull never --wait
```
