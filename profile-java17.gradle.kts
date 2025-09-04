//java 17
//deploy to maven repo: ./gradlew clean build -Pjdk=java17 -Ptarget=8

val targetVersion = project.findProperty("target")?.toString()?.toInt() ?: 17

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.toVersion(targetVersion)
  targetCompatibility = JavaVersion.toVersion(targetVersion)
}

//jacoco {
//  toolVersion = "0.8.7"
//}
configure<JacocoPluginExtension> {
  toolVersion = "0.8.7"
}

configure<SourceSetContainer> {
  named("main") {
    java.srcDir("src/main/java17+")
  }
  named("test") {
    java.srcDir("src/test/java17+")
  }
}

if (targetVersion > 8) {
  tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED")
  }
}

tasks.withType<Test> {
  jvmArgs("--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED")
}
tasks.withType<JavaExec> {
  // jvmArgs("--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED")
}
