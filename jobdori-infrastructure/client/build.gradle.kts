dependencies {
    // Modules
    api(project(":jobdori-core"))

    // RestClient
    implementation(libs.spring.web)
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
