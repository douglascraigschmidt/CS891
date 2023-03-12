dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.springframework:spring-test")
    implementation ("org.junit.jupiter:junit-jupiter")
    testImplementation ("org.springframework.boot:spring-boot-starter-test")
    runtimeOnly("com.h2database:h2")
    implementation("commons-dbcp:commons-dbcp:1.4")
}
