plugins {
  java
  `java-library`
  signing
  checkstyle
  id("com.github.hierynomus.license") version "0.16.1"
  jacoco
  `maven-publish`
  id("com.vanniktech.maven.publish") version "0.37.0"
  id("com.github.ben-manes.versions") version "0.52.0"  // ./gradlew dependencyUpdates
}

group = "com.github.harbby"
version = "2.0.0"  //SNAPSHOT

val jdk = project.findProperty("jdk")?: "java21"  //default -Pjdk=java11
//val jdk: def = System.getProperty("jdk") ?: " -Djdk=java11
apply(from = "profile-${jdk}.gradle.kts")

sourceSets {
  main {
    java {
      srcDirs("src/main/$jdk")
    }
    resources {
      srcDirs("src/main/resources", "src/main/profile/$jdk")
    }
  }

  test {
    java {
      srcDirs("src/test/java", "src/test/$jdk")
    }
  }
}

repositories {
  mavenLocal()
  mavenCentral()
}

tasks.processResources {
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
tasks.withType<org.gradle.jvm.tasks.Jar> { duplicatesStrategy = DuplicatesStrategy.INCLUDE }

object versions {
  val jna = "5.17.0"
  val asm = "9.8"
  val jackson = "2.20.0"
  val javassist = "3.30.2-GA"
  val junit = "5.13.4"
  val jmh = "1.37"
  val jansi = "2.4.2"
}

dependencies {
  compileOnly("net.java.dev.jna:jna-platform-jpms:${versions.jna}")
  implementation("org.ow2.asm:asm:${versions.asm}")
  compileOnly("com.fasterxml.jackson.core:jackson-databind:${versions.jackson}")
  compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${versions.jackson}")

  testImplementation("org.javassist:javassist:${versions.javassist}")
  testImplementation("org.junit.jupiter:junit-jupiter-api:${versions.junit}")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${versions.junit}")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("org.fusesource.jansi:jansi:${versions.jansi}")
  testImplementation("org.openjdk.jmh:jmh-core:${versions.jmh}")
  testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:${versions.jmh}")
}

// ./gradlew test --tests "com.github.harbby.gadtry.graph.GraphxTest"
// ./gradlew test --tests "com.github.harbby.gadtry.graph.GraphxTest.testCreateGraph1" -PshowStandardStreams=true
tasks.withType<Test> {
  useJUnitPlatform()
  //testLogging.showStandardStreams = true
  testLogging.showStandardStreams = project.findProperty("showStandardStreams")?.toString()?.toBoolean()?: false
}

configurations {
  testImplementation {
    extendsFrom(compileOnly.get())
  }
}

tasks.jar {
  manifest {
    // Set the required manifest attributes for the Java agent, cf.
    // https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html.
    attributes(
            "Premain-Class" to "com.github.harbby.gadtry.jvm.JvmAgent",
            "Can-Redefine-Classes" to true,
            "Can-Retransform-Classes" to true
    )
  }
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
  options {
    encoding = "UTF-8"
    (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
  }
}

checkstyle {
  toolVersion = "9.3"
  isShowViolations = true
  configFile = rootProject.file("src/checkstyle/ci_checks.xml")
  //configFile = file("${rootDir}/src/checkstyle/ci_checks.xml")
}
//assemble.dependsOn 'checkstyle'

tasks.named<Checkstyle>("checkstyleMain") {
  configFile = rootProject.file("src/checkstyle/ci_checks.xml")
  source("src")
  include("**/*.java")
  exclude("**/gen/**", "**/test/**", "**/build/**", "**/module-info.java", "**/TimSort.java")
  classpath = files()
}

license {
  headerDefinitions {  //see: http://code.mycila.com/license-maven-plugin/#supported-comment-types
    register("javadoc_style") {
      firstLine = "/*"
      endLine = " */"
      beforeEachLine = " * "
      afterEachLine = ""
      firstLineDetectionPattern = "(\\s|\\t)*/\\*.*\$"
      lastLineDetectionPattern = ".*\\*/(\\s|\\t)*\$"
      allowBlankLines = false
      padLines = false
      //skipLine = "//"
      isMultiline = true
    }
  }
  header = rootProject.file("src/license/LICENSE-HEADER.txt")
  strictCheck = true
  excludes(listOf("**/*.properties", "**/*.sql", "**/*.txt", "**/com/github/harbby/gadtry/base/TimSort.java",))
  //include "**/*.java"
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(false)  //xml.enabled = true
    csv.required.set(false)
    html.required.set(true) //html.enabled = true
  }

  classDirectories.setFrom(files(classDirectories.files.map {
    fileTree(it) {
      exclude(
              "**/com/github/harbby/gadtry/base/TimSort.class",
              "**/com/github/harbby/gadtry/spi/ServiceLoad.class",
              "**/com/github/harbby/gadtry/collection/IntArrayBuffer**",
              "**/com/github/harbby/gadtry/base/JavaParameterizedTypeImpl.class",
              "**/com/github/harbby/gadtry/base/Platform**"
      )
    }
  }))
}
tasks.check {
  dependsOn(tasks.jacocoTestReport)
}

tasks.javadoc {
  isFailOnError = false
}

// Release to Maven Central via the Central Portal (bytecode must stay Java 8):
//   ./gradlew clean publishToMavenCentral -Pjdk=java21 -Ptarget=8
// then review and press "Publish" at https://central.sonatype.com/publishing/deployments
// ~/.gradle/gradle.properties: mavenCentralUsername/mavenCentralPassword (Portal user token),
// signing.keyId/signing.password/signing.secretKeyRingFile
val hasSigningKey = project.hasProperty("signing.keyId") || project.hasProperty("signingInMemoryKey")

mavenPublishing {
  publishToMavenCentral(automaticRelease = false)
  // publishToMavenLocal works without a key; Central releases are guarded below
  if (hasSigningKey) {
    signAllPublications()
  }

  pom {
    name.set("gadtry")
    description.set("Gadtry A collection of java tool libraries. Contains: ioc. aop. exec. graph ...")
    url.set("https://github.com/harbby/gadtry")

    licenses {
      license {
        name.set("The Apache License, Version 2.0")
        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("repo")
      }
    }
    developers {
      developer {
        id.set("harbby")
        name.set("harbby")
        email.set("yezhixinghai@gmail.com")
      }
    }
    scm {
      url.set("https://github.com/harbby/gadtry")
      connection.set("scm:git:https://github.com/harbby/gadtry.git")
      developerConnection.set("scm:git:ssh://git@github.com/harbby/gadtry.git")
    }
  }
}

gradle.taskGraph.whenReady {
  val releasing = allTasks.any { it.name.contains("MavenCentral") }
  if (releasing && java.targetCompatibility != JavaVersion.VERSION_1_8) {
    throw GradleException("Maven Central releases must target Java 8, add -Ptarget=8 (current: ${java.targetCompatibility})")
  }
  if (releasing && !hasSigningKey) {
    throw GradleException("Maven Central requires signed artifacts, configure signing.keyId/signing.password/signing.secretKeyRingFile")
  }
  if (releasing && !(project.hasProperty("mavenCentralUsername") && project.hasProperty("mavenCentralPassword"))) {
    val hint = if (project.hasProperty("mavenUsername")) " (found legacy mavenUsername/mavenPassword, rename them to mavenCentralUsername/mavenCentralPassword)" else ""
    throw GradleException("Missing mavenCentralUsername/mavenCentralPassword (Central Portal user token) in ~/.gradle/gradle.properties$hint")
  }
}

tasks.register("install") {
  dependsOn(tasks.publishToMavenLocal)
}
tasks.register("upload") {
  dependsOn("publishToMavenCentral")
}
