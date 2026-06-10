# Jobdori

- 잡도리 (WEB 1팀)

## 기술 스택

- Kotlin 2.3.21 (JDK 25)
- Spring Boot 4.0.6
- PostgreSQL, H2
- Spring WebMVC REST API, Spring GraphQL
- Spring Data JPA, Kotlin JDSL
- Spring REST Docs, Asciidoctor
- Kotest, SpringMockK

## 모듈 구조

ISSUE-007에서 기존 `jobdori-domain` 중심 구조를 `core`와 `infrastructure`로 분리했습니다.

```text
컴파일 의존:
jobdori-api → jobdori-core → jobdori-common
jobdori-infrastructure → jobdori-core → jobdori-common

런타임 결합:
jobdori-api ⇢(runtimeOnly) jobdori-infrastructure
```

- `jobdori-api`: 애플리케이션 진입점, REST API, GraphQL resolver/schema, request/response DTO, API 예외 처리, REST Docs.
- `jobdori-core`: 유스케이스 Service, Repository/Client 추상화, 순수 도메인 모델과 도메인 예외.
- `jobdori-infrastructure`: JPA 엔티티, Repository 구현, persistence mapper, 외부 API client 구현, JPA/DB 설정.
- `jobdori-common`: 공통 에러/예외, JSON/로깅 유틸리티, 공통 상수.

`jobdori-api`는 `jobdori-infrastructure`를 컴파일타임에 직접 의존하지 않고 `runtimeOnly`로만 결합합니다. 실제 제어 흐름은
`api → core → infrastructure`로 이어지지만, 컴파일 의존은 `infrastructure → core` 방향으로 둡니다.

## 빌드 및 실행

```bash
# 전체 빌드
./gradlew bootJar

# API 서버 실행
./gradlew :jobdori-api:bootRun

# 전체 테스트
./gradlew test

# 모듈별 테스트
./gradlew :jobdori-api:test
./gradlew :jobdori-core:test
./gradlew :jobdori-infrastructure:test
./gradlew :jobdori-common:test

# REST Docs 문서 생성
./gradlew :jobdori-api:asciidoctor
```
