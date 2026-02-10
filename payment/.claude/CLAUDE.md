# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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
│   │   ├── handler/      # 도메인 이벤트 핸들러
│   │   └── outbox/       # Outbox 패턴 (공통 추상화 + 구현체)
│   └── refund/         # 환불 처리
├── domain/             # 순수 도메인 로직 (Spring 의존성 없음)
│   ├── payment/        # Payment 애그리거트 + 도메인 이벤트
│   └── refund/         # Refund 값 객체
├── infra/              # 인프라스트럭처 어댑터
│   ├── payment/        # JPA 리포지토리, 외부 게이트웨이, outbox JPA 구현
│   └── schedule/       # 재시도 스케줄러
├── presentation/       # REST 컨트롤러
├── common/             # 공통 유틸리티 (Money, 매퍼)
└── global/             # 예외 핸들러
```

### 핵심 디자인 패턴

#### 1. 트랜잭션 분리
`PaymentService`가 플로우를 오케스트레이션합니다. 외부 결제 게이트웨이 호출은 **트랜잭션 밖**에서 수행하고, `PaymentTransactionService`(`@Transactional`)가 결과를 영속화하며 도메인 이벤트를 발생시킵니다.

#### 2. 도메인 이벤트 → Outbox
`PaymentTransactionService`에서 트랜잭션이 커밋되면 `PaymentEventHandler`가 `@TransactionalEventListener(AFTER_COMMIT)`으로 이벤트를 처리합니다:
- `PaymentApprovedEvent` → `TicketIssueOutboxService.issueOrEnqueue()` (티켓 발급)
- `PaymentFailedEvent` → `ReservationCancelOutboxService.cancelOrEnqueue()` (예약 취소)

#### 3. Outbox 패턴 (공통 추상화)
`application/payment/outbox/` 하위에 공통 추상화와 두 가지 구현체가 있습니다:

- **공통**: `OutboxTarget` 인터페이스, `OutboxRepository<T>` 포트, `AbstractOutboxRetryUseCase<T>` (Template Method)
- **TicketIssue**: 결제 성공 → 티켓 발급 (`ticketissue/`)
- **ReservationCancel**: 결제 실패 → 예약 취소 (`cancel/`)

`AbstractOutboxRetryUseCase`는 50건 배치로 PENDING 항목을 조회하고, 실패 시 `failAndBackoff()`로 지수 백오프 재시도합니다. 에러 메시지는 500자로 잘라서 저장합니다.

새 outbox를 추가하려면: `OutboxTarget` 구현 → `OutboxRepository` 포트 구현 → `AbstractOutboxRetryUseCase` 상속 → 스케줄러 추가.

#### 4. 서킷 브레이커
외부 서비스 어댑터(`TicketingAdaptor`, `ReservationCancelAdaptor`)에 Resilience4j `@CircuitBreaker`를 적용하여 장애 전파를 방지합니다. Fallback 메서드에서 CB OPEN 상태와 일반 실패를 구분하여 서로 다른 예외(`*TemporarilyUnavailableException` / `*ExternalCallFailedException`)를 던집니다.

### 출력 포트 (port/output)

| 포트 | 용도 | 어댑터 |
|------|------|--------|
| `PaymentGateway` | PG사 결제 승인 | `HttpPaymentGatewayAdaptor`, `StubPaymentGatewayAdaptor` |
| `TicketingClient` | 티켓 발급 (SRT 서비스) | `TicketingAdaptor` (CircuitBreaker) |
| `ReservationCancelClient` | 예약 취소 | `ReservationCancelAdaptor` (CircuitBreaker) |
| `PaymentRepository` | 결제 데이터 영속화 | `JpaPaymentRepository` |
| `ReservationReader` | 예약 정보 조회 | infra 내 구현체 |

### 도메인 모델

- **Payment**: 상태(`REQUESTED → SUCCEEDED/FAILED`)를 가진 애그리거트 루트. 최대 금액 1,000만원 검증. `approve()` 시 `PaymentApprovedEvent`, `fail()` 시 `PaymentFailedEvent` 발행.
- **Refund**: 콘텐츠 시작 시간까지 남은 시간에 따라 정책 기반 환불 금액을 계산하는 값 객체.
- **Money**: 금액을 표현하는 값 객체.

## 테스트

- 통합 테스트는 `infra.config.AbstractIntegrationTest`를 상속:
  - `PostgresSQLContainerInitializer` 인터페이스로 Testcontainers PostgreSQL 공유 컨테이너 제공
  - `@DynamicPropertySource`로 JDBC 접속 정보 주입
  - `@BeforeAll`에서 `schema-test.sql` 실행 (IF NOT EXISTS로 멱등)
  - `@Transactional`로 테스트 후 자동 롤백
  - `@ActiveProfiles("test")`, `@TestInstance(PER_CLASS)`
- HTTP 어댑터 테스트에 `MockWebServer` 사용
- 테스트 픽스처: `domain/fixture/` 패키지 (`TestPaymentFactory`, `PaymentEntityFixture`, `RefundEntityFixture`)
- 스케줄러는 `@Profile("!test")`로 테스트 시 비활성화

## 설정

- **프로필 구조**: `dev`/`prod` 메인 프로필 + `-db`, `-cb-test`, `logging-*` 보조 프로필을 `spring.profiles.include`로 조합
- **데이터베이스**: PostgreSQL, `ddl-auto: none` (스키마는 외부에서 관리)
- **환경 변수**: `PAYMENT_GATEWAY_URL`, `PERFORMANCE_SERVICE_URL`(SRT 서비스), `toss.secret-key`
- **재시도 설정**: `ticket.issue.retry.delay-ms`, `reservation.cancel.retry.delay-ms` (기본 5000ms)

## 네이밍 컨벤션

- `*Service`: 비즈니스 로직 (`PaymentService`, `TicketIssueOutboxService`)
- `*TransactionService`: 트랜잭션 경계 담당 (`PaymentTransactionService`)
- `*Adaptor`: 출력 포트 구현체 (`TicketingAdaptor`, `ReservationCancelAdaptor`)
- `*RetryUseCase`: Outbox 재시도 로직 (`TicketIssueRetryUseCase`)
- `*ExternalCallFailedException` / `*TemporarilyUnavailableException`: 외부 호출 실패 예외