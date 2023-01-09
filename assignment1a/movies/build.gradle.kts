plugins {
    id("java")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-test:5.3.22")
    implementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

/**
 * For multiple microservices in one project you can't
 * have a single bootJar task since there will be many
 * boot JARs. The tasks provide individual bootJar tasks
 * for each service and a aggregation of those tasks in
 * a single bootJars task that can be used externally.
 */
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
        useJUnitPlatform()
    }
}
