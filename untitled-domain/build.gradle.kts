dependencies {
    // Core
    api(project(":untitled-common"))

    // Spring
    implementation(libs.spring.boot.starter)

    // JPA
    api(libs.spring.boot.starter.data.jpa)

    // JDSL
    implementation(libs.kotlin.jdsl.jpql.dsl)
    implementation(libs.kotlin.jdsl.jpql.render)
    implementation(libs.kotlin.jdsl.spring.data.jpa)

    // DB
    runtimeOnly(libs.h2)
    implementation(libs.spring.boot.h2console)
    runtimeOnly(libs.postgresql)
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
