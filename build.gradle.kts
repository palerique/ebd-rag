plugins {
    java
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "br.com.palerique"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
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
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("dev.langchain4j:langchain4j:0.33.0")
//    implementation("dev.langchain4j:langchain4j-easy-rag:0.33.0")
    implementation("dev.langchain4j:langchain4j-ollama:0.33.0")
    implementation("dev.langchain4j:langchain4j-document-parser-apache-tika:0.34.0")
    implementation("dev.langchain4j:langchain4j-web-search-engine-tavily:0.34.0")
    implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2-q:0.33.0")
//    implementation("com.github.langchain4j:langchain4j-core:1.0.0")
//    implementation("com.github.langchain4j:langchain4j-embedding-store-inmemory:1.0.0")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
//    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
