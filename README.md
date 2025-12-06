# 🎫 NextFrame: 공연장 좌석 예매 서비스

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/javase/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-28.2.2-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)

> **"대규모 트래픽을 감당할 수 있는 견고한 티켓팅 서비스"**
>
> NextFrame은 사용자가 원하는 공연의 일정과 좌석을 선택하고 결제하여 QR 티켓을 발급받는 예매 플랫폼입니다.

---

## 📝 Table of Contents
1. [Links](#-links)
2. [Project Overview](#-project-overview)
3. [Team Members](#-team-members)
4. [Tech Stack](#-tech-stack)
5. [System Architecture & ERD](#-system-architecture--erd)
6. [Trouble Shooting](#-trouble-shooting-performance--issue)
    - [1. 좌석 예매 시 동시성 이슈 해결 (Race Condition)](#1-좌석-예매-시-동시성-이슈-해결-race-condition)
    - [2. 좌석 조회 성능 최적화 및 Full GC 해결 (Caching)](#2-좌석-조회-성능-최적화-및-full-gc-해결-caching)
    - [3. DB 커넥션 고갈 해결 및 트랜잭션 범위 최소화](#3-db-커넥션-고갈-해결-및-트랜잭션-범위-최소화)
7. [Collaboration Rules](#-collaboration-rules-ground-rules)

---

## 🔗 Links
- **Service URL:** [https://nextframe.wisoft.dev/](https://nextframe.wisoft.dev/)
- **API Documentation:** [Swagger UI Docs](https://next-frame-lab.github.io/swagger-ui-docs/)

---

## 📅 Project Overview
### 기획 배경
티켓팅 서비스는 특정 시간에 트래픽이 증가하는 대표적인 도메인입니다. **NextFrame**은 단순한 기능 구현을 넘어, 대규모 트래픽 상황에서도 데이터 정합성을 유지하고 안정적인 서비스를 제공할 수 있는 견고한 백엔드 시스템을 구축하기 위해 시작되었습니다.

### 주요 기능
*   소셜 로그인, 공연 검색/목록/상세 조회, 좌석 선택 및 예매
*   결제, QR 티켓 발급, 리뷰 작성(좋아요)

---

## 👥 Team Members
| 이름 | Role | GitHub | 담당 역할 |
|:---:|:---:|:---:|:---|
| **김민서** | Team Leader <br> Backend | [@git-mesome](https://github.com/git-mesome) | • **좌석 결제 및 환불 프로세스 구현**<br>• 티켓(QR 코드) 발급 시스템<br>• 공연 검색 기능 고도화 (QueryDSL)<br>• DB/배포/모니터링 환경 구축 및 DB 설계 |
| **안진표** | Team Member <br> Backend | [@Jinpyo-An](https://github.com/Jinpyo-An) | • **공연 좌석 및 예매 프로세스 구현**<br>• 소셜 로그인(OAuth2) 및 JWT 인증/인가<br>• 공연 검색 및 리뷰(좋아요) 기능<br>• 배포 환경 구축 및 API 설계 |

---

## 🛠 Tech Stack

### Backend
*   **Language:** Java 21
*   **Framework:** Spring Boot 3.5.4
*   **Test:** JUnit 5
*   **ORM:** JPA, QueryDSL

### Database
*   **RDBMS:** PostgreSQL

### Infrastructure & Deployment
*   **Server:** On-Premise
*   **Virtualization:** Docker
*   **CI/CD:** GitHub Actions
*   **Web Server:** Nginx

### Collaboration Tools
*   Discord, Notion, Swagger UI

---

## 📐 System Architecture & ERD
> 현재 아키텍처와 DB 설계도는 지속적으로 고도화 중입니다.

### System Architecture
*(이미지 첨부 예정)*

### ERD (Entity Relationship Diagram)
*(이미지 첨부 예정)*

---

## 🔥 Trouble Shooting (Performance & Issue)

### 1. 좌석 예매 시 동시성 이슈 해결 (Race Condition)

**🛑 문제 상황 (Situation)**
선착순 예매 테스트를 진행하던 중, 단일 좌석에 대해 여러 명의 사용자가 동시에 예매를 시도할 경우 중복 예약이 발생하는 현상이 확인되었습니다.

> * **기대 결과:** 1개의 좌석에 1명만 예매 성공
> * **실제 결과:** 1개의 좌석에 N명이 예매 성공 (데이터 정합성 깨짐)

**🔍 원인 분석 (Cause)**
애플리케이션 레벨에서 **'좌석 조회(SELECT)'** 와 **'예매 상태 변경(UPDATE)'** 사이에 텀이 존재하여, 트랜잭션이 커밋되기 전에 다른 트랜잭션이 동일한 좌석 정보를 읽어가는 **경쟁 상태(Race Condition)** 가 원인이었습니다.

**💡 해결 과정 (Action)**
데이터의 무결성을 최우선으로 고려하여 **DB 레벨에서의 강력한 락(Lock)** 과 제약 조건을 활용하는 전략을 선택했습니다.

1.  **Pessimistic Lock (비관적 락) 도입**
    * 충돌이 빈번하게 일어날 것으로 예상되는 선착순 예매 특성상, 데이터 조회 시점부터 락을 거는 `PESSIMISTIC_WRITE` 모드를 적용했습니다.
    * 이를 통해 `SELECT ... FOR UPDATE` 쿼리가 실행되어, 트랜잭션이 끝날 때까지 다른 트랜잭션의 접근을 차단했습니다.
2.  **UNIQUE 제약 조건 추가 (Defense in Depth)**
    * 락을 적용했더라도 만약의 사태를 대비해 DB 테이블 컬럼에 `UNIQUE` 제약 조건을 추가하여 물리적으로 중복 데이터가 `INSERT` 되는 것을 원천 차단했습니다.

**✅ 결과 (Result)**
nGrinder를 이용한 동시성 테스트 결과, 동일 좌석에 대한 다중 요청 시 단 1건의 트랜잭션만 성공하고 나머지는 롤백됨을 확인했습니다.
**데이터 정합성 100%를 보장**하는 안정적인 예매 시스템을 구축했습니다.

### 2. 좌석 조회 성능 최적화 및 Full GC 해결 (Caching)

**🛑 문제 상황 (Situation)**
좌석 상태 조회 기능에 대한 부하 테스트(Load Test)를 진행하던 중, 주기적으로 **TPS가 급락하고 응답 시간이 비정상적으로 급증**하는 불안정한 현상이 관측되었습니다.

**🔍 원인 분석 (Cause)**
서버 모니터링 및 **GC(Garbage Collection) 로그를 분석**한 결과, 요청 처리 과정에서 불필요한 객체가 과도하게 생성되어 **빈번한 Full GC**가 발생했고, 이로 인한 'Stop-the-world' 현상이 성능 저하의 주원인임을 확인했습니다.

**💡 해결 과정 (Action)**
DB I/O 부하를 줄이고 객체 생성 자체를 최소화하기 위해 **캐싱(Caching) 전략**을 도입했습니다.

1.  **캐시 정책(Policy) 수립**
    * 메모리 효율성을 고려하여 적절한 만료 시간(TTL)과 최대 저장 크기(Maximum Size)를 설정했습니다.
2.  **Spring Cache 적용 (@Cacheable)**
    * 빈번하게 호출되는 좌석 조회 메서드에 `@Cacheable`을 적용하여, 동일한 요청에 대해서는 DB 접근 없이 메모리에서 즉시 응답하도록 개선했습니다.
3.  **데이터 정합성 보장 (@CacheEvict)**
    * 좌석 예매로 인해 상태가 변경되는 시점에 `@CacheEvict`를 실행하여 기존 캐시를 즉시 제거함으로써, 사용자가 항상 최신 좌석 정보를 볼 수 있도록 정합성을 보장했습니다.

**✅ 결과 (Result)**
캐싱 적용 후 재테스트 결과, **최대 TPS가 약 167% 향상**되었으며 응답 시간이 대폭 감소했습니다. 또한, Full GC 발생 빈도가 현저히 줄어들어 안정적인 서비스 운영이 가능해졌습니다.

### 3. DB 커넥션 고갈 해결 및 트랜잭션 범위 최소화

**🛑 문제 상황 (Situation)**
좌석 예매 부하 테스트 진행 중, 다수의 트랜잭션이 **10초 이상 소요되는 '느린 응답(Outlier)'** 현상이 관찰되었으며, 시스템 전체의 처리량(TPS)이 급격히 저하되었습니다.

**🔍 원인 분석 (Cause)**
**DB 커넥션 풀(HikariCP) 고갈**이 핵심 원인이었습니다.
* **분석 결과:** 실제 SQL 처리 시간은 약 1.13초였으나, 커넥션을 얻기 위한 **대기 시간(Connection Wait)이 무려 9.7초**에 달했습니다.
* **병목 지점:** 1.13초라는 긴 트랜잭션 시간(DB 락 포함) 동안 스레드가 커넥션을 점유하고 있어, 후속 요청들이 커넥션을 획득하지 못하고 대기 상태에 빠지는 악순환이 발생했습니다.

**💡 해결 과정 (Action)**
**"스레드가 DB 커넥션을 점유하는 시간을 줄인다"** 는 목표로 최적화를 수행했습니다.

1.  **트랜잭션 범위(@Transactional) 최소화**
    * 단순 읽기/검증 로직을 트랜잭션 범위 밖으로 분리했습니다.
    * 실제 DB 쓰기 작업만 수행하는 로직을 별도로 분리하여 트랜잭션 유지 시간을 단축했습니다.
2.  **락 전략 변경 (Pessimistic → Optimistic)**
    * 기존 비관적 락(Pessimistic Lock)의 대기 시간(Lock Wait) 문제를 해결하기 위해 **낙관적 락(Optimistic Lock)** 으로 전환했습니다.
    * `@Version`을 적용하여 충돌 감지 메커니즘을 도입하고, `OptimisticLockingFailureException` 예외 처리를 통해 동시성 문제를 제어했습니다.
3.  **인프라 튜닝**
    * DB 커넥션 풀(HikariCP) 사이즈를 30으로 조정하여 처리 용량을 최적화했습니다.

**✅ 결과 (Result) 및 향후 계획**
리팩토링 결과, **개별 DB 트랜잭션 점유 시간을 획기적으로 단축**했습니다.
하지만 동기(Sync) 방식의 구조적 한계로 인한 성능 병목이 여전히 존재함을 확인하였으며, 이를 근본적으로 해결하기 위해 다음과 같은 계획을 세웠습니다.

* **비동기 아키텍처 전환:** 대기열 큐(Queue) 및 비동기(Async) 처리를 도입하여 사용자 요청과 DB 처리를 분리
* **인프라 확장:** 늘어나는 트래픽을 감당하기 위한 **DB 서버 증설(Scale-out) 및 이중화** 구성

---

## 🤝 Collaboration Rules (Ground Rules)
NextFrame 팀은 명확한 규칙을 통해 코드 품질을 유지하고 협업 효율을 높입니다.

### 1. Git Strategy
*   **Git Flow** 전략을 따릅니다. (`main`, `develop`, `feature`, `release`, `hotfix`)
*   **Merge Strategy:** `Rebase and Merge`를 사용하여 커밋 히스토리를 깔끔하게 관리합니다.

### 2. Code Convention
*   [Naver Java Coding Convention](https://naver.github.io/hackday-conventions-java/)을 준수합니다.

### 3. Commit Message Convention
이슈 트래킹을 위해 아래 템플릿을 엄격히 준수합니다.

```text
#<이슈번호> <타입>(<범위>): <제목> (제목은 40자 이내)

<본문> (선택 사항, 한 줄 띄우고 작성. 72자 이내로 줄 바꿈)

Resolves: #<이슈번호>
See also: None (관련 항목이 있으면 #<이슈번호> 기입)