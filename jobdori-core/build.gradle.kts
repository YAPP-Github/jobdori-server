dependencies {
    api(project(":jobdori-common"))
    implementation(libs.spring.boot.starter)
    implementation("org.springframework:spring-tx")// spring-context + spring-tx (@Service/@Transactional)
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
