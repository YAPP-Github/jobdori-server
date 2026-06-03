import org.gradle.internal.execution.caching.CachingState.enabled

dependencies {
    // Core
    implementation(project(":untitled-domain"))

    // Web
    implementation(libs.spring.boot.starter.webmvc)
    testImplementation(libs.spring.boot.starter.webmvc.test)

    // GraphQL
    implementation(libs.spring.boot.starter.graphql)
    testImplementation(libs.spring.boot.starter.graphql.test)

    // Validation
    implementation(libs.spring.boot.starter.validation)
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}

application {
    mainClass.set("com.untitled.api.ApiApplicationKt")
}
