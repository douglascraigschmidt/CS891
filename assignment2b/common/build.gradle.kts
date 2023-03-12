tasks {
    bootJar {enabled = false}
    jar {enabled = true}
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}