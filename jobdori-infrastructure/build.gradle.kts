dependencies {
    api(project(":jobdori-core"))

    implementation(libs.spring.boot.starter)
    api(libs.spring.boot.starter.data.jpa)

    // 외부 API 연동(OpenAI 등) — RestClient
    implementation("org.springframework:spring-web")

    implementation(libs.kotlin.jdsl.jpql.dsl)
    implementation(libs.kotlin.jdsl.jpql.render)
    implementation(libs.kotlin.jdsl.spring.data.jpa)

    runtimeOnly(libs.h2)
    implementation(libs.spring.boot.h2console)
    runtimeOnly(libs.postgresql)
}

allOpen {                                   // ⚠️ 기존은 javax.* → jakarta.* 로 정정
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
