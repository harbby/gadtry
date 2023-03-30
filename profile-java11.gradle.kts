//java 11
//deploy to maven repo: ./gradlew clean build -Pjdk=java11 -Ptarget=8

val targetVersion = project.findProperty("target")?.toString()?.toInt() ?: 11

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.toVersion(targetVersion)
  targetCompatibility = JavaVersion.toVersion(targetVersion)
}

configure<SourceSetContainer> {
  named("main") {
    java.srcDir("src/main/java11+")
  }
  named("test") {
    java.srcDir("src/test/java11+")
  }
}

if (targetVersion > 8) {
  tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("--add-exports=java.base/sun.nio.ch=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED")
  }
}

tasks.withType<Test> {
  jvmArgs("--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED")
  jvmArgs("--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED")
  jvmArgs("--add-opens=java.base/jdk.internal.loader=ALL-UNNAMED")
}
tasks.withType<JavaExec> {
  // jvmArgs("--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED")
}
