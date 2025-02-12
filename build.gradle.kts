plugins {
  java
  `java-library`
  signing
  checkstyle
  id("com.github.hierynomus.license") version "0.16.1"
  jacoco
  `maven-publish`
}

group = "com.github.harbby"
version = "1.10.9-SNAPSHOT"  //SNAPSHOT

val jdk = project.findProperty("jdk")?: "java11"  //default -Pjdk=java11
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
  val jna = "5.13.0"
  val asm = "9.6"
  val jackson = "2.15.3"
  val javassist = "3.29.2-GA"
  val junit = "5.9.3"
  val jmh = "1.37"
  val jansi = "2.4.0"
}

dependencies {
  compileOnly("net.java.dev.jna:jna-platform-jpms:${versions.jna}")
  implementation("org.ow2.asm:asm:${versions.asm}")
  compileOnly("com.fasterxml.jackson.core:jackson-databind:${versions.jackson}")
  compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${versions.jackson}")

  testImplementation("org.javassist:javassist:${versions.javassist}")
  testImplementation("org.junit.jupiter:junit-jupiter-api:${versions.junit}")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${versions.junit}")
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

tasks.register<Jar>("sourcesJar") {
  dependsOn(tasks.classes)
  archiveClassifier.set("sources")
  from(sourceSets.main.orNull?.allSource)
}

tasks.register<Jar>("javadocJar") {
  dependsOn(tasks.javadoc)
  archiveClassifier.set("javadoc")
  from(tasks.javadoc.orNull?.destinationDir)
  tasks.javadoc.orNull?.isFailOnError = false
}

//--- gradle clean build publish
publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(project.components.getByName("java"))
      //artifact jar
      artifact(tasks.getByName("sourcesJar"))
      artifact(tasks.getByName("javadocJar"))

      version = "${project.version}"
      artifactId = project.name
      groupId = "${project.group}"

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
          connection.set("https://github.com/harbby/gadtry.git")
          developerConnection.set("https://github.com/harbby/gadtry.git")
        }
      }
    }
  }
  repositories {
    maven {
      credentials {
        username = project.findProperty("mavenUsername")?.toString()
        password = project.findProperty("mavenPassword")?.toString()
      }
      // change URLs to point to your repos, e.g. http://my.org/repo
      val repository_url = if (project.version.toString().endsWith("-SNAPSHOT"))
              "https://oss.sonatype.org/content/repositories/snapshots" else
              "https://oss.sonatype.org/service/local/staging/deploy/maven2"
      url = uri(repository_url)
    }
    mavenLocal()
  }
  artifacts {
    archives(tasks.getByName("sourcesJar"))
    archives(tasks.getByName("javadocJar"))
  }
  signing {
    isRequired = project.hasProperty("mavenUsername")
    sign(publishing.publications.getByName("mavenJava"))
  }

  tasks.register("install") {
    dependsOn(tasks.publishToMavenLocal)
  }
  tasks.register("upload") {
    dependsOn(tasks.publish)
  }
}
