plugins {
    java
    id("org.springframework.boot") version "3.5.13"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.12.0"
}

group = "org.abdrafikov"
version = "0.0.1-SNAPSHOT"
description = "groupbuy"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

openApiGenerate {
    inputSpec.set("$rootDir/src/main/resources/api.yaml")
    generatorName.set("spring")
    apiPackage.set("org.abdrafikov.groupbuy.api.generated.api")
    modelPackage.set("org.abdrafikov.groupbuy.api.generated.dto")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useJakartaEe" to "true",
            "useBeanValidation" to "true",
            "useResponseEntity" to "true",
            "useTags" to "true",
            "openApiNullable" to "false",
            "skipDefaultInterface" to "true"
        )
    )
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generate-resources/main/src/main/java"))
        }
    }
}

tasks.compileJava {
    dependsOn("openApiGenerate")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
