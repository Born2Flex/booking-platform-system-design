// Applied to every deployable Spring Boot service.
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("booking.java-conventions")
    id("org.springframework.boot")
}

dependencies {
    // Spring Boot BOM as a Gradle platform (instead of the io.spring.dependency-management plugin).
    val springBootBom = platform(SpringBootPlugin.BOM_COORDINATES)
    implementation(springBootBom)
    annotationProcessor(springBootBom)
    developmentOnly(springBootBom)
}

// Services are not consumed by other modules, so only the executable boot jar is needed.
tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<BootJar>("bootJar") {
    // Predictable name for the Dockerfile: build/libs/<service-name>.jar
    archiveFileName = "${project.name}.jar"

    // Docker image layers, ordered from least to most frequently changing.
    // Our own libs/* get their own "modules" layer, so a code change in a service
    // only rebuilds the small "application" layer.
    layered {
        application {
            intoLayer("spring-boot-loader") { include("org/springframework/boot/loader/**") }
            intoLayer("application")
        }
        dependencies {
            intoLayer("modules") { includeProjectDependencies() }
            intoLayer("snapshot-dependencies") { include("*:*:*SNAPSHOT") }
            intoLayer("dependencies")
        }
        layerOrder = listOf("dependencies", "spring-boot-loader", "snapshot-dependencies", "modules", "application")
    }
}
