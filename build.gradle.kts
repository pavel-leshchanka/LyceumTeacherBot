plugins {
	id ("java")
	id ("org.springframework.boot") version ("3.3.3")
	id ("io.spring.dependency-management") version ("1.1.6")
}

val telegrambotsVersion = "7.9.1"

group = "by.faeton"
version = "1.0.0-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation ("org.springframework.boot:spring-boot-starter")
	implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation ("com.h2database:h2")
	implementation ("org.telegram:telegrambots-springboot-longpolling-starter:$telegrambotsVersion")
	implementation ("org.telegram:telegrambots-client:$telegrambotsVersion")
	implementation ("com.google.api-client:google-api-client:2.7.0")
	implementation ("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
	implementation ("com.google.apis:google-api-services-sheets:v4-rev20240826-2.0.0")
	implementation ("javax.xml.bind:jaxb-api:2.3.1")
	implementation ("jakarta.validation:jakarta.validation-api")
	compileOnly ("org.projectlombok:lombok")
	annotationProcessor ("org.projectlombok:lombok")
}