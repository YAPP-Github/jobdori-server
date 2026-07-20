dependencies {
    // Modules
    api(project(":jobdori-core"))

    // RestClient
    implementation(libs.spring.web)
    // Datadog LLM Observability 수동 계측 (-javaagent 없으면 no-op)
    implementation(libs.dd.trace.api)
    // JD 정적 크롤러(HTML 파싱)
    implementation(libs.jsoup)
    // Test
    testImplementation(libs.mockwebserver)
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
