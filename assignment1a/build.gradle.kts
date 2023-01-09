plugins {
    id("org.springframework.boot") version "2.7.3" apply (true)
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    id("java")
}

extra["springCloudVersion"] = "2021.0.4"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

tasks {
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
        jvmArgs =
            listOf(
                "--enable-preview",
                "--add-modules",
                "jdk.incubator.concurrent"
            )
    }
}

subprojects {
    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("java")
    }

    // All Java/Kotlin versions are defined here.
    java.sourceCompatibility = JavaVersion.VERSION_19
    java.targetCompatibility = JavaVersion.VERSION_19

    group = "edu.vandy.recommender"
    version = "1.0.0"

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
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
        testImplementation("junit:junit")

        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("org.springframework.boot:spring-boot-starter-parent:2.7.3")
        developmentOnly("org.springframework.boot:spring-boot-devtools")
        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org-mockito")
        }
    }
}
