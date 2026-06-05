import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("application")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.asciidoctor.jvm.convert)
}

allprojects {
    group = "com.untitled"

    repositories {
        mavenCentral()
    }
}

application {
    mainClass.set("com.untitled")
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "application")
    apply(plugin = "java-library")
    apply(plugin = "java-test-fixtures")
    apply(plugin = "org.asciidoctor.jvm.convert")

    kotlin {
        jvmToolchain(25)
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xannotation-default-target=param-property",
            )
        }
    }

    val libs = rootProject.libs

    dependencies {
        // Kotlin
        implementation(libs.kotlin.reflect)
        implementation(libs.kotlin.stdlib.jdk8)

        // Logging
        implementation(libs.kotlin.logging)

        // Test
        testImplementation(libs.spring.boot.starter.test)
        testImplementation(libs.kotest.runner.junit)
        testImplementation(libs.kotest.assertions.core)
        testImplementation(libs.kotest.extensions.spring)
        testImplementation(libs.springmockk)
    }

    tasks.withType<Test> {
        group = "verification"
        useJUnitPlatform()

        testClassesDirs = sourceSets.named("test").get().output.classesDirs
        classpath = sourceSets.named("test").get().runtimeClasspath

        testLogging {
            showExceptions = true
            exceptionFormat = FULL
            showCauses = true
            showStackTraces = true
            events = setOf(FAILED)
        }
    }

    tasks.register<Test>("docsTest") {
        group = "verification"
        description = "Run Docs tests"
        useJUnitPlatform()

        systemProperty("kotest.tags", "docs-test")
    }
}
