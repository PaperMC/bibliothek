import net.kyori.indra.repository.sonatypeSnapshots

plugins {
  val indraVersion = "2.0.6"

  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.license-header") version indraVersion
  id("org.springframework.boot") version "2.4.5"
  id("java")
}

group = "io.papermc"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  sonatypeSnapshots()
}

indra {
  javaVersions {
    target(11)
  }

  github("PaperMC", "bibliothek")
  mitLicense()
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  checkstyle("ca.stellardrift:stylecheck:0.1")
  implementation("com.vdurmont:semver4j:3.1.0")
  implementation("net.kyori:coffee-functional:1.0.0-SNAPSHOT")
  implementation("org.springdoc:springdoc-openapi-ui:1.5.12")
  implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
  implementation("org.springframework.boot:spring-boot-starter-undertow")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-web") {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat") // we use undertow
  }
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }
}

tasks {
  bootJar {
    launchScript()
  }

  // From StackOverflow: https://stackoverflow.com/a/53087407
  // Licensed under: CC BY-SA 4.0
  // Adapted to Kotlin
  register<Copy>("buildForDocker") {
    from(bootJar)
    into("build/libs/docker")
    rename { fileName ->
      // a simple way is to remove the "-$version" from the jar filename
      // but you can customize the filename replacement rule as you wish.
      fileName.replace("-$version", "")
    }
  }
}
