
springBoot.mainClass.set("edu.vandy.recommender.DatabaseClientDriver")

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-test-autoconfigure")
    implementation("org.springframework:spring-test")
    implementation ("org.junit.jupiter:junit-jupiter")
    implementation("org.assertj:assertj-core:3.24.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.1.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("io.projectreactor:reactor-test")
    testImplementation(project(mapOf("path" to ":gateway")))
}
