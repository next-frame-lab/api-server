# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 다룰 때 참고하는 가이드입니다.

## 빌드 및 테스트 명령어

```bash
# 프로젝트 빌드
./gradlew build

# 테스트 실행 (Testcontainers로 PostgreSQL 사용)
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "wisoft.nextframe.payment.domain.PaymentTest"

# 단일 테스트 메서드 실행
./gradlew test --tests "wisoft.nextframe.payment.domain.PaymentTest.testMethodName"

# 애플리케이션 실행
./gradlew bootRun
```

## 아키텍처 개요

Spring Boot 3.5, Java 21 기반의 **결제 마이크로서비스**이며, **헥사고날 아키텍처**(Ports & Adapters)를 따릅니다.

### 레이어 구조

```
src/main/java/wisoft/nextframe/payment/
├── application/        # 유스케이스 및 오케스트레이션
│   ├── payment/        # 결제 승인 플로우
│   │   ├── port/output/  # 출력 포트 (인터페이스)
│   │   └── handler/      # 도메인 이벤트 핸들러
│   ├── refund/         # 환불 처리
│   └── ticketissue/    # Outbox 패턴 기반 티켓 발급
├── domain/             # 순수 도메인 로직 (Spring 의존성 없음)
│   ├── payment/        # Payment 애그리거트
│   └── refund/         # Refund 값 객체
├── infra/              # 인프라스트럭처 어댑터
│   ├── payment/        # JPA 리포지토리, 외부 게이트웨이
│   ├── ticketissue/    # 티켓팅 서비스 클라이언트
│   └── schedule/       # 재시도 스케줄러
├── presentation/       # REST 컨트롤러
├── common/             # 공통 유틸리티 (Money, 매퍼)
└── global/             # 예외 핸들러
```

### 핵심 디자인 패턴

1. **도메인 이벤트**: `Payment.approve()` 호출 시 `PaymentApprovedEvent`가 발행되고, 트랜잭션 커밋 후 `PaymentEventHandler`가 이를 처리하여 티켓 발급을 트리거합니다.

2. **Outbox 패턴**: `TicketIssueOutboxService`가 티켓팅 서비스에 대한 외부 호출의 신뢰성을 보장하며, 스케줄러를 통한 재시도를 지원합니다.

3. **트랜잭션 분리**: `PaymentService`가 플로우를 오케스트레이션하며 외부 결제 게이트웨이 호출은 트랜잭션 밖에서 수행하고, `PaymentTransactionService`가 트랜잭션 내 영속화를 담당합니다.

4. **서킷 브레이커**: `TicketingAdaptor`가 Resilience4j를 사용하여 외부 SRT 티켓팅 서비스의 장애 전파를 방지합니다.

### 도메인 모델

- **Payment**: 상태(REQUESTED → SUCCEEDED/FAILED)를 가진 애그리거트 루트. 유효성 검증 규칙(최대 금액 1,000만원)과 도메인 이벤트를 포함합니다.
- **Refund**: 콘텐츠 시작 시간까지 남은 시간에 따라 정책 기반 환불 금액을 계산하는 값 객체입니다.
- **Money**: 금액을 표현하는 값 객체입니다.

### 외부 연동

- **결제 게이트웨이**: 결제 승인을 위한 외부 PG (토스 스타일) (`PaymentGateway` 포트 → `HttpPaymentGatewayAdaptor`/`StubPaymentGatewayAdaptor`)
- **티켓팅 서비스 (SRT)**: 결제 성공 후 티켓 발급 (`TicketingClient` 포트 → `TicketingAdaptor`)

## 테스트

- 통합 테스트는 `AbstractIntegrationTest`를 상속하며 다음을 제공합니다:
  - Testcontainers PostgreSQL (단일 공유 컨테이너)
  - `src/test/resources/schema-test.sql`에서 스키마 초기화
  - `@ActiveProfiles("test")` 설정
- HTTP 어댑터 테스트에 `MockWebServer` 사용
- 테스트 픽스처는 `domain/fixture/` 패키지에 위치

## 설정

- 프로필: `dev`, `prod`, `test`
- 데이터베이스: PostgreSQL, `ddl-auto: none` (스키마는 외부에서 관리)
- Actuator 엔드포인트: `/actuator/health`, `/actuator/prometheus`