plugins {
    id("java")
    id("org.springframework.boot") version ("3.3.3")
    id("io.spring.dependency-management") version ("1.1.6")
}

group = "by.faeton"
version = "1.1.6-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")

    compileOnly("org.projectlombok:lombok")

    implementation("com.google.api-client:google-api-client:2.7.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20240826-2.0.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.telegram:telegrambots-client:7.9.1")
    implementation("org.telegram:telegrambots-springboot-longpolling-starter:7.9.1")
}