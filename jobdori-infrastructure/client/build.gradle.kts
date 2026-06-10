dependencies {
    api(project(":jobdori-core"))

    implementation(libs.spring.boot.starter)
    implementation("org.springframework:spring-web")   // RestClient
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
