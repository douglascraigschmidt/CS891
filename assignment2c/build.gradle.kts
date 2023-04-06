import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.1" apply (true)
    id("io.spring.dependency-management") version "1.1.0"
    id("java")
    id("io.franzbecker.gradle-lombok") version "4.0.0"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.6.21" apply (false)
}

extra["springCloudVersion"] = "2022.0.0"

tasks {
    bootJar { enabled = false }
    jar { enabled = false }
}

subprojects {
    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
    }

    // All Java/Kotlin versions are defined here.
    java {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    tasks.findByName("check")?.dependsOn("clean")

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "19"
            }
        }

        withType<JavaCompile>().configureEach {
            options.compilerArgs.addAll(
                listOf(
                    "--enable-preview",
                    "--add-modules",
                    "jdk.incubator.concurrent"
                )
            )
            targetCompatibility = "19"
        }

        withType<Test>().configureEach {
            ignoreFailures = true
            useJUnitPlatform()
            jvmArgs =
                listOf(
                    "--enable-preview",
                    "--add-modules",
                    "jdk.incubator.concurrent",
                    // For mockk workaround for JDK 17+ static mocks issues.
                    "--add-opens",
                    "java.base/java.util.concurrent=ALL-UNNAMED",
                    "--add-opens",
                    "java.base/java.net=ALL-UNNAMED",
                    "--add-opens",
                    "java.base/jdk.incubator.concurrent=ALL-UNNAMED"
                )
        }
    }

    group = "edu.vandy.recommender"
    version = "1.0.0"

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    dependencyManagement {
        imports {
            mavenBom(
                "org.springframework.cloud:spring-cloud-dependencies:${
                    property(
                        "springCloudVersion"
                    )
                }"
            )
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("org.springframework.boot:spring-boot-starter-parent:2.7.3")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        testImplementation("junit:junit")
        testImplementation("org.assertj:assertj-core:3.24.1")
        testImplementation("io.projectreactor:reactor-test")

        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org-mockito")
        }
        testImplementation("com.ninja-squad:springmockk:3.1.1")
        testImplementation(project(":testing"))
    }

    configure<SourceSetContainer> {
        named("main") {
            java.srcDir("src/main/java")
        }
    }
}

//if (file("$projectDir/admin/skeleton.gradle").isFile) {
//    apply(from = "$projectDir/admin/skeleton.gradle")
//}

