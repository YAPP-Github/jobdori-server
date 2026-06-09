# AGENTS.md

This file provides guidance to Codex when working with code in this repository.

## 빌드 및 실행

```bash
# 전체 빌드(JAR 생성)
./gradlew bootJar

# API 서버 실행
./gradlew :jobdori-api:bootRun

# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :jobdori-api:test
./gradlew :jobdori-core:test
./gradlew :jobdori-infrastructure:test
./gradlew :jobdori-common:test

# 단일 테스트 클래스 실행
./gradlew :jobdori-api:test --tests "com.jobdori.api.application.sample.SampleResolverTest"

# REST Asciidoctor 문서 생성
./gradlew :jobdori-api:asciidoctor
```

## 모듈 구조

ISSUE-007 이후 4개 모듈의 계층형 + 의존성 역전 구조:

```
컴파일 의존:
jobdori-api → jobdori-core → jobdori-common
jobdori-infrastructure → jobdori-core → jobdori-common

런타임 결합:
jobdori-api ⇢(runtimeOnly) jobdori-infrastructure
```

- **jobdori-common**: 공통 에러 코드/예외 계층, JSON/로깅 유틸리티, 공통 상수 등을 관리한다.
- **jobdori-core**: application 유스케이스, Service 인터페이스/구현, Repository/Client 추상화, 순수 도메인 모델과 도메인 예외를 관리한다.
- **jobdori-infrastructure**: JPA 엔티티, Spring Data/JDSL Repository 구현, persistence mapper, 외부 API client 구현, JPA/DB 설정,
  infrastructure test fixtures를 관리한다.
- **jobdori-api**: Spring Boot 애플리케이션 진입점, Web MVC REST API, GraphQL resolver/schema, request/response DTO, API 예외 처리,
  REST Docs 문서를 관리한다.

런타임 제어 흐름은 `api → core(Service) → Repository/Client 인터페이스 → infrastructure 구현체`로 흐르지만, 컴파일 의존 방향은
`infrastructure → core`이다. `core → infrastructure` 의존은 순환 의존과 core 순수성 파괴를 만들기 때문에 금지한다.

## 기술 스택

- **언어/프레임워크**: Kotlin 2.3.21 + Spring Boot 4.0.6 + JDK 25
- **ORM**: Spring Data JPA + Kotlin JDSL
- **DB**: PostgreSQL
- **API**: Spring WebMVC REST API + Spring GraphQL
- **문서화**: Spring REST Docs + Asciidoctor
- **테스트**: Kotest + SpringMockK

## 작업 규칙

- 모듈 컴파일 의존 방향(`api → core → common`, `infrastructure → core → common`)을 깨지 않는다.
- `jobdori-api`는 `jobdori-infrastructure`를 직접 import하지 않는다. `jobdori-api/build.gradle.kts`에서는
  `runtimeOnly(project(":jobdori-infrastructure"))`만 허용하고 `implementation`/`api` 의존은 금지한다.
- `jobdori-core`는 JPA, DB, 외부 API 구현 세부를 몰라야 한다. core에는 Repository/Client 인터페이스와 유스케이스 로직만 둔다.
- DB 접근은 core `{Feature}Repository` 인터페이스와 infrastructure `{Feature}RepositoryImpl` 구현으로 분리한다.
  Spring Data Repository는 `{Feature}JpaRepository` 이름을 사용한다.
- 외부 연동은 core `{Feature}Client` 인터페이스와 infrastructure `{Feature}ClientImpl` 구현으로 분리한다.
- mapper는 출처를 접두로 명시한다. 예: `SamplePersistenceMapper`, `SampleExternalMapper`.
- support 패키지는 모듈 루트 `support/{기술}`에 둔다. 예: `api/support/{rest,graphql}`, `core/support/spring`,
  `infrastructure/support/jpa`.
- REST API 변경 시 필요한 경우 `jobdori-api/src/docs/asciidoc` 문서와 REST Docs 테스트를 함께 갱신한다.
- GraphQL API 변경 시 `jobdori-api/src/main/resources/graphql/schema.graphqls`와
  `jobdori-api/src/test/resources/graphql-test`의 query/mutation 파일을 함께 확인한다.
