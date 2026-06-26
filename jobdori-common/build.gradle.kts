dependencies {
    // CommonsLang3
    api(libs.commons.lang3)

    // Jackson
    api(libs.jackson.core)
    api(libs.jackson.databind)
    api(libs.jackson.module.kotlin)

    // PDF
    implementation(libs.pdfbox)
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
