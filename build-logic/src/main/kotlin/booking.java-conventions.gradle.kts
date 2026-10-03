// Applied to every Java module (services and libs).
plugins {
    java
}

group = "com.bookingplatform"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all,-processing,-serial"))
}

// Fast unit tests in src/test, slower Spring/Testcontainers tests in src/integrationTest.
//   ./gradlew test             -> inner loop
//   ./gradlew integrationTest  -> before push / CI
//   ./gradlew check            -> both
testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
        }
        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter()
            // Use main's compiled classes directly (not its jar): no packaging step, and works
            // for services whose plain jar is disabled.
            sources {
                compileClasspath += sourceSets.main.get().output
                runtimeClasspath += sourceSets.main.get().output
            }
            targets.all {
                testTask.configure { shouldRunAfter(tasks.named("test")) }
            }
        }
    }
}

configurations.named("integrationTestImplementation") { extendsFrom(configurations.testImplementation.get()) }
configurations.named("integrationTestRuntimeOnly") { extendsFrom(configurations.testRuntimeOnly.get()) }

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}
