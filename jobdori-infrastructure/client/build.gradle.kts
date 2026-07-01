dependencies {
    // Modules
    api(project(":jobdori-core"))

    // RestClient
    implementation(libs.spring.web)
    // Test
    testImplementation(libs.mockwebserver)
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
