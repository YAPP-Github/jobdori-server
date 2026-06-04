import java.time.LocalDate
import java.time.ZoneId

dependencies {
    // Core
    implementation(project(":untitled-domain"))

    // Web
    implementation(libs.spring.boot.starter.webmvc)
    testImplementation(libs.spring.boot.starter.webmvc.test)

    // GraphQL
    implementation(libs.spring.boot.starter.graphql)
    testImplementation(libs.spring.boot.starter.graphql.test)

    // Validation
    implementation(libs.spring.boot.starter.validation)

    // Spring Rest Docs
    testImplementation(libs.spring.boot.restdocs)
    testImplementation(libs.spring.restdocs.mockmvc)
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
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
            include("css/**")
        }
    }

    doFirst {
        project.delete(file("build/docs/asciidoc"))
    }
}

application {
    mainClass.set("com.untitled.api.ApiApplicationKt")
}
