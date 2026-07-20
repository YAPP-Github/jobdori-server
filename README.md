# Jobdori

- 잡도리 (28기 WEB 1팀)

## 링크

- [API Docs](https://yapp-github.github.io/jobdori-server)

## 기술 스택

- Backend: Kotlin 2, JDK 25, Spring Boot 4
- API: REST API, GraphQL
- Database: PostgreSQL
- Persistence: Spring Data JPA, Kotlin JDSL
- Test & Docs: Kotest, Spring REST Docs
- Infrastructure: AWS Elastic Beanstalk, Terraform
- CI/CD: GitHub Actions

## 모듈 구조

- `jobdori-api`: 애플리케이션 진입점, REST API, GraphQL resolver/schema, request/response DTO, API 예외 처리, REST Docs.
- `jobdori-core`: 유스케이스 Service, Repository/Client 추상화, 순수 도메인 모델과 도메인 예외.
- `jobdori-infrastructure`: JPA 엔티티, Repository 구현, persistence mapper, 외부 API client 구현, JPA/DB 설정.
- `jobdori-common`: 공통 에러/예외, JSON/로깅 유틸리티, 공통 상수.
