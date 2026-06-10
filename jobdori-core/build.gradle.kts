dependencies {
    // Modules
    api(project(":jobdori-common"))

    // Spring
    api(libs.spring.boot.starter)
    implementation(libs.spring.tx)
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
