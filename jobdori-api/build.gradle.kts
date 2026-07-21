import java.time.LocalDate
import java.time.ZoneId

// dd-java-agent는 앱 클래스패스가 아니라 EB 번들에 -javaagent용으로만 들어간다
val ddJavaAgent: Configuration by configurations.creating {
    isTransitive = false
}

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
    ddJavaAgent(libs.dd.java.agent)

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
    from(ddJavaAgent) {
        rename { "dd-java-agent.jar" }
    }
    from(project.file("Procfile"))
    from(project.file(".platform")) {
        into(".platform")
    }
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
