import java.time.LocalDate
import java.time.ZoneId

dependencies {
    // Modules
    implementation(project(":jobdori-core"))
    implementation(project(":jobdori-common"))
    runtimeOnly(project(":jobdori-infrastructure:persistence"))
    runtimeOnly(project(":jobdori-infrastructure:client"))
    testImplementation(testFixtures(project(":jobdori-core")))

    // Web
    implementation(libs.spring.boot.starter.webmvc)
    testImplementation(libs.spring.boot.starter.webmvc.test)

    // GraphQL
    implementation(libs.spring.boot.starter.graphql)
    testImplementation(libs.spring.boot.starter.graphql.test)

    // Validation
    implementation(libs.spring.boot.starter.validation)

    // Monitoring
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.sentry.logback)
    implementation(libs.sentry.spring.boot.starter)

    // Spring Rest Docs
    testImplementation(libs.spring.boot.restdocs)
    testImplementation(libs.spring.restdocs.mockmvc)
    testImplementation(libs.pdfbox)
}

tasks.bootJar {
    enabled = true
    archiveFileName.set("application.jar")
}

tasks.jar {
    enabled = true
}

tasks.register<Zip>("elasticBeanstalkBundle") {
    group = "distribution"
    description = "Build the Elastic Beanstalk application source bundle"
    dependsOn(tasks.bootJar)

    archiveFileName.set("jobdori-api-elastic-beanstalk.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    from(tasks.bootJar.flatMap { it.archiveFile })
    from(project.file("Procfile"))
}

tasks.docsTest {
    outputs.dir("build/generated-snippets")
}

tasks.asciidoctor {
    inputs.dir("build/generated-snippets")
    setOutputDir(file("build/docs/asciidoc"))
    dependsOn(tasks.docsTest)
    baseDirFollowsSourceFile()

    sources {
        include("index.adoc")
        include("restapi/index.adoc")
        include("graphql/index.adoc")
    }

    attributes(
        mapOf(
            "snippets" to file("build/generated-snippets"),
            "revnumber" to LocalDate.now(ZoneId.of("Asia/Seoul")).toString()
        ),
    )

    resources {
        from("src/docs/asciidoc") {
            include("index.css")
        }
    }

    doFirst {
        project.delete(file("build/docs/asciidoc"))
    }
}

application {
    mainClass.set("com.jobdori.api.ApiApplicationKt")
}
