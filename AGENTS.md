# AGENTS.md

This file provides guidance to Codex when working with code in this repository.

## 빌드 및 실행

```bash
# 전체 빌드(JAR 생성)
./gradlew bootJar

# API 서버 실행
./gradlew :untitled-api:bootRun

# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :untitled-api:test
./gradlew :untitled-domain:test
./gradlew :untitled-common:test

# 단일 테스트 클래스 실행
./gradlew :untitled-api:test --tests "com.untitled.api.application.sample.SampleResolverTest"

# REST Asciidoctor 문서 생성
./gradlew :untitled-api:asciidoctor
```

## 모듈 구조

3개 모듈의 단방향 의존 구조:

```
untitled-api → untitled-domain → untitled-common
```

- **untitled-common**: 공통 에러 코드/예외 계층, JSON/로깅 유틸리티, 공통 상수 등을 관리한다.
- **untitled-domain**: JPA 엔티티, 도메인 모델, Repository, JPA/DB 설정, domain test fixtures 등을 관리한다.
- **untitled-api**: Spring Boot 애플리케이션 진입점, Web MVC REST API, GraphQL resolver/schema, request/response DTO, API 예외 처리, REST Docs 문서를 관리한다.

## 기술 스택

- **언어/프레임워크**: Kotlin 2.3.21 + Spring Boot 4.0.6 + JDK 25
- **ORM**: Spring Data JPA + Kotlin JDSL
- **DB**: PostgreSQL
- **API**: Spring WebMVC REST API + Spring GraphQL
- **문서화**: Spring REST Docs + Asciidoctor
- **테스트**: Kotest + SpringMockK

## 작업 규칙

- 모듈 의존 방향(`api → domain → common`)을 깨지 않는다.
- REST API 변경 시 필요한 경우 `untitled-api/src/docs/asciidoc` 문서와 REST Docs 테스트를 함께 갱신한다.
- GraphQL API 변경 시 `untitled-api/src/main/resources/graphql/schema.graphqls`와 `untitled-api/src/test/resources/graphql-test`의 query/mutation 파일을 함께 확인한다.
