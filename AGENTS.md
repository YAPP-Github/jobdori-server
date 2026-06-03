# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## 빌드 및 실행

```bash
# 전체 빌드
./gradlew bootJar

# API 서버 실행 (포트 20000, 기본 프로필: local)
./gradlew :untitled-api:bootRun

# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :untitled-api:test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.untitled.api.application.health.HealthCheckApiTest"
```

## 모듈 구조

3개 모듈의 단방향 의존 구조:

```
untitled-api → untitled-domain → untitled-common
```

- **untitled-common**: 에러 코드/예외 계층, 유틸리티 등
- **untitled-domain**: 도메인, 엔티티, Repository 등
- **untitled-api**: REST API, GraphQL

## 기술 스택

- **언어/프레임워크**: Kotlin 2.3 + Spring Boot 4.0 + JDK 25
- **ORM**: Spring Data JPA + Kotlin JDSL
- **DB**: H2 (local/test), PostgreSQL
- **테스트**: Kotest 6 + SpringMockK
