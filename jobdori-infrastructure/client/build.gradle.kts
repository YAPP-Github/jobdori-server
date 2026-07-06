dependencies {
    // Modules
    api(project(":jobdori-core"))

    // RestClient
    implementation(libs.spring.web)
    // JD 정적 크롤러(HTML 파싱)
    implementation(libs.jsoup)
    // Test
    testImplementation(libs.mockwebserver)
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
