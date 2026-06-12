import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.util.jar.JarFile
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

buildscript {
    dependencies {
        classpath("org.openapitools:openapi-generator-gradle-plugin:7.11.0")
    }
}

plugins {
    java
    id("application")
    id("org.springframework.boot") version "3.4.13"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.11.0"
    id("com.google.cloud.tools.jib") version "3.4.4"
    id("com.diffplug.spotless") version "7.0.4" apply true
    id("net.ltgt.errorprone") version "3.1.0"
    id("com.avast.gradle.docker-compose") version "0.16.11" apply false
}

application {
    mainClass = "com.domeni.kapita.payment.KapitaPaymentApplication"
}

version = "0.0.0-SNAPSHOT"
group = "com.domeni.kapita.payment"

val kapitaPlatformVersion =
    providers
        .gradleProperty("kapitaPlatformVersion")
        .orElse(providers.environmentVariable("KAPITA_PLATFORM_VERSION"))
        .orElse("0.1.2-SNAPSHOT")
        .get()

val nexusMavenPublicUrl =
    providers
        .gradleProperty("nexusMavenPublicUrl")
        .orElse(providers.environmentVariable("NEXUS_MAVEN_PUBLIC_URL"))
        .orElse(providers.environmentVariable("NEXUS_MAVEN_URL"))
        .orElse(providers.environmentVariable("NEXUS_MAVEN_RELEASES_URL"))
        .orElse(providers.environmentVariable("NEXUS_MAVEN_SNAPSHOTS_URL"))
        .orNull

val nexusUsername =
    providers
        .gradleProperty("nexusUsername")
        .orElse(providers.environmentVariable("NEXUS_CREDENTIALS_USR"))
        .orNull

val nexusPassword =
    providers
        .gradleProperty("nexusPassword")
        .orElse(providers.environmentVariable("NEXUS_CREDENTIALS_PSW"))
        .orNull

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    if (!nexusMavenPublicUrl.isNullOrBlank()) {
        maven {
            url = uri(nexusMavenPublicUrl)
            isAllowInsecureProtocol = nexusMavenPublicUrl.startsWith("http://")
            credentials {
                username = nexusUsername ?: "admin"
                password = nexusPassword ?: "9d912f7d-c29a-4795-bd0a-b17481659304"
            }
        }
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("verifyBootJarStartClass") {
    group = "verification"
    description = "Verifies bootJar has a valid Start-Class manifest entry."
    dependsOn(tasks.named("bootJar"))
    doLast {
        val bootJar =
            tasks
                .named<BootJar>("bootJar")
                .get()
                .archiveFile
                .get()
                .asFile
        JarFile(bootJar).use { jar ->
            val startClass = jar.manifest.mainAttributes.getValue("Start-Class")
            check(!startClass.isNullOrBlank()) {
                "Start-Class is missing in ${bootJar.name}"
            }
            check(!startClass.contains("/")) {
                "Start-Class must be a fully-qualified class name, got: $startClass"
            }
            check(!startClass.endsWith(".java")) {
                "Start-Class must not be a source file path, got: $startClass"
            }
        }
    }
}

tasks.named("check") {
    dependsOn("verifyBootJarStartClass")
}

val springCloudVersion = "2024.0.3"
val mapstructVersion = "1.6.3"
val cucumberVersion = "7.20.1"
val lombokVersion = "1.18.42"
val errorProneVersion = "2.48.0"
val nullAwayVersion = "0.13.1"
val mockitoVersion = "5.22.0"
val byteBuddyVersion = "1.18.7"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

dependencies {
    implementation(platform("com.domeni.kapita:kapita-platform-bom:$kapitaPlatformVersion"))
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.domeni.kapita:kapita-kafka-inbound-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("com.domeni.kapita:kapita-security-jwt-starter")
    implementation("com.domeni.kapita:kapita-jpa-eclipselink-starter")
    implementation("org.liquibase:liquibase-core")
    // Lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
    // Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // Mapstruct
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.javamoney:moneta:1.4.4")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("net.bytebuddy:byte-buddy:$byteBuddyVersion")
    testRuntimeOnly("net.bytebuddy:byte-buddy-agent:$byteBuddyVersion")

    // DB
    implementation("org.postgresql:postgresql")

    implementation("org.jspecify:jspecify:1.0.0")
    errorprone("com.google.errorprone:error_prone_core:$errorProneVersion")
    errorprone("com.uber.nullaway:nullaway:$nullAwayVersion")

    // OPENAPI
    implementation("io.swagger:swagger-annotations:1.6.8")
    implementation("org.openapitools:jackson-databind-nullable:0.2.3")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")

    testImplementation("io.rest-assured:rest-assured:5.3.2")
    testImplementation("io.rest-assured:spring-mock-mvc:5.4.0")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-spring:$cucumberVersion")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:postgresql")
}

val errorproneClasspath by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(configurations.errorprone.get())
}

tasks.withType<JavaCompile> {
    doFirst {
        var jarsToMoveToClasspathNames =
            listOf(
                "error_prone_core",
                "error_prone_annotations",
                "error_prone_check_api",
                "nullaway",
                "jsr305",
                "aopalliance",
                "dataflow",
            )
        val jarsForClasspath =
            errorproneClasspath.files.filter { jarFile ->
                jarsToMoveToClasspathNames.any { jarName -> jarFile.name.startsWith(jarName) }
            }
        val originalModulePath = configurations.compileClasspath.get().files
        val filteredModulePath = originalModulePath.minus(jarsForClasspath.toSet())
        val newClasspath = classpath.plus(jarsForClasspath)
        options.compilerArgs.addAll(
            listOf(
                "--module-path",
                filteredModulePath.joinToString(separator = File.pathSeparator) { it.absolutePath },
            ),
        )
        // Set the new classpath
        classpath = files(newClasspath)
    }
    options.compilerArgs.addAll(
        listOf(
            "--enable-preview",
            "--should-stop=ifError=FLOW",
        ),
    )
    options.errorprone {
        disableWarningsInGeneratedCode = true
        excludedPaths.set(".*/build/generated/sources/.*")
        disableAllChecks.set(true)
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:AnnotatedPackages", "com.domeni.kapita.payment")
        option("NullAway:JSpecifyMode", true)
        option("NullAway:TreatGeneratedAsUnannotated", true)
        option("NullAway:CheckOptionalEmptiness", true)
        option("NullAway:SuggestSuppressions", true)
        option(
            "NullAway:ExcludedClasses",
            listOf(
                "com.domeni.kapita.payment.KapitaPaymentApplication",
                "com.domeni.kapita.payment.config",
            ).joinToString(","),
        )
        option(
            "NullAway:ExcludedClassAnnotations",
            listOf(
                "org.mapstruct.Mapper",
                "org.springframework.boot.test.context.SpringBootTest",
                "org.junit.jupiter.api.Test",
                "org.junit.jupiter.api.extension.ExtendWith",
                "org.junit.jupiter.api.BeforeEach",
                "org.mockito.Mock",
                "org.springframework.context.annotation.Configuration",
                "org.springframework.boot.autoconfigure.SpringBootApplication",
                "jakarta.annotation.Generated",
                "com.fasterxml.jackson.annotation.JsonProperty",
                "jakarta.persistence.Converter",
            ).joinToString(","),
        )
        option(
            "NullAway:CustomNullableAnnotations",
            listOf(
                "org.springframework.lang.Nullable",
                "jakarta.annotation.Nullable",
            ).joinToString(","),
        )
        option(
            "NullAway:CustomNonnullAnnotations",
            listOf(
                "org.springframework.lang.NonNull",
                "jakarta.annotation.Nonnull",
            ).joinToString(","),
        )
        option("NullAway:HandleTestAssertionLibraries", true)
        option(
            "NullAway:ExcludedFieldAnnotations",
            listOf(
                "org.springframework.beans.factory.annotation.Autowired",
                "jakarta.inject.Inject",
                "lombok.Generated",
                "lombok.NonNull",
                "org.jspecify.annotations.NonNull",
            ).joinToString(","),
        )
    }
}

tasks.named<JavaCompile>("compileTestJava") {
    options.compilerArgs.add("--enable-preview")
    options.errorprone.isEnabled.set(false)
}

tasks.test {
    useJUnitPlatform {
        excludeTags("e2e", "data")
    }
    jvmArgs("--enable-preview")
    testLogging {
        showStandardStreams = true
    }
}

tasks.register<Test>("dataTest") {
    ignoreFailures = true
    dependsOn("assemble", "testClasses")
    useJUnitPlatform {
        includeTags("data")
    }
    jvmArgs("--enable-preview")
}

tasks.register<JavaExec>("e2eTest") {
    group = "verification"
    description = "Runs end-to-end tests with Cucumber."
    dependsOn("testClasses")
    mainClass.set("io.cucumber.core.cli.Main")
    classpath = sourceSets.test.get().runtimeClasspath
    jvmArgs("--enable-preview")
    args =
        listOf(
            "--plugin",
            "pretty",
            "--plugin",
            "html:build/cucumber-reports/html/index.html",
            "--plugin",
            "json:build/cucumber-reports/json/cucumber.json",
            "--plugin",
            "junit:build/cucumber-reports/json/cucumber.xml",
            "--tags",
            "@e2e and not @Disabled",
            "--glue",
            "com.domeni.kapita.payment.e2e",
            "src/test/resources/features",
        )
}

tasks.register<GenerateTask>("mainOpenApiGenerate") {
    generatorName = "spring"
    templateDir.set("$rootDir/openapi/templates/spring-boot")
    inputSpec = "$rootDir/openapi/main.yaml"
    outputDir =
        layout.buildDirectory
            .dir("generated/sources/openapi")
            .get()
            .asFile.path
    apiPackage = "com.domeni.kapita.generated.payment.api"
    modelPackage = "com.domeni.kapita.generated.payment.dto"
    configOptions =
        mapOf(
            "dateLibrary" to "java8-localdatetime",
            "library" to "spring-boot",
            "interfaceOnly" to "true",
            "useTags" to "true",
            "skipDefaultInterface" to "true",
            "useSpringBoot3" to "true",
            "openApiNullable" to "false",
        )
    typeMappings =
        mapOf(
            "time" to "java.time.LocalTime",
            "date" to "java.time.LocalDate",
            "date-time" to "java.time.LocalDateTime",
        )
    importMappings =
        mapOf(
            "LocalTime" to "java.time.LocalTime",
            "LocalDate" to "java.time.LocalDate",
            "LocalDateTime" to "java.time.LocalDateTime",
        )
    val generatedSourceCodeDir = file(outputDir.get() + "/src/main/java/com/domeni/kapita/generated/payment")
    doFirst {
        generatedSourceCodeDir.deleteRecursively()
    }
    onlyIf {
        !generatedSourceCodeDir.exists() ||
            file(inputSpec.get()).lastModified() > generatedSourceCodeDir.lastModified()
    }
}

tasks.register<GenerateTask>("monetbilOpenApiGenerate") {
    generatorName = "spring"
    templateDir.set("$rootDir/openapi/templates/spring-http-interface")
    inputSpec = "$rootDir/openapi/monetbil.yaml"
    outputDir =
        layout.buildDirectory
            .dir("generated/sources/monetbil")
            .get()
            .asFile.path
    apiPackage = "com.domeni.kapita.generated.monetbil.api"
    modelPackage = "com.domeni.kapita.generated.monetbil.dto"
    configOptions =
        mapOf(
            "dateLibrary" to "java8-localdatetime",
            "library" to "spring-boot",
            "interfaceOnly" to "true",
            "useTags" to "true",
            "skipDefaultInterface" to "true",
            "useSpringBoot3" to "true",
            "openApiNullable" to "false",
        )
}

tasks.register<GenerateTask>("authentisUserEventOpenApiGenerate") {
    generatorName = "spring"
    templateDir.set("$rootDir/openapi/templates/spring-boot")
    inputSpec = "$rootDir/openapi/authentis-user.yaml"
    outputDir =
        layout.buildDirectory
            .dir("generated/sources/authentis-user-event")
            .get()
            .asFile.path
    apiPackage = "com.domeni.kapita.generated.payment.event.api"
    modelPackage = "com.domeni.kapita.generated.payment.event.dto"
    configOptions =
        mapOf(
            "dateLibrary" to "java8-localdatetime",
            "library" to "spring-boot",
            "interfaceOnly" to "true",
            "useTags" to "true",
            "skipDefaultInterface" to "true",
            "useSpringBoot3" to "true",
            "openApiNullable" to "false",
        )
    typeMappings =
        mapOf(
            "time" to "java.time.LocalTime",
            "date" to "java.time.LocalDate",
            "date-time" to "java.time.LocalDateTime",
        )
    importMappings =
        mapOf(
            "LocalTime" to "java.time.LocalTime",
            "LocalDate" to "java.time.LocalDate",
            "LocalDateTime" to "java.time.LocalDateTime",
        )
    val generatedSourceCodeDir =
        file(outputDir.get() + "/src/main/java/com/domeni/kapita/generated/payment/event")
    doFirst {
        generatedSourceCodeDir.deleteRecursively()
    }
    onlyIf {
        !generatedSourceCodeDir.exists() ||
            file(inputSpec.get()).lastModified() > generatedSourceCodeDir.lastModified()
    }
}

tasks.compileJava.get().dependsOn(
    tasks["mainOpenApiGenerate"],
    tasks["authentisUserEventOpenApiGenerate"],
)

sourceSets.main.get().java.srcDir(
    layout.buildDirectory
        .dir("generated/sources/openapi/src/main/java")
        .get()
        .asFile.path,
)
sourceSets.main.get().java.srcDir(
    layout.buildDirectory
        .dir("generated/sources/monetbil/src/main/java")
        .get()
        .asFile.path,
)
sourceSets.main.get().java.srcDir(
    layout.buildDirectory
        .dir("generated/sources/authentis-user-event/src/main/java")
        .get()
        .asFile.path,
)

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat().aosp()
        importOrder()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        replaceRegex("Stacktrace", "Throwable\\.printStackTrace\\(\\)", "log.error(\"\", e)")
    }
}
