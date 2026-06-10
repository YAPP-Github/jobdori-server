dependencies {
    api(project(":jobdori-core"))

    implementation(libs.spring.boot.starter)
    api(libs.spring.boot.starter.data.jpa)

    implementation(libs.kotlin.jdsl.jpql.dsl)
    implementation(libs.kotlin.jdsl.jpql.render)
    implementation(libs.kotlin.jdsl.spring.data.jpa)

    runtimeOnly(libs.h2)
    implementation(libs.spring.boot.h2console)
    runtimeOnly(libs.postgresql)
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
