//java 8
//deploy to maven repo: ./gradlew clean build -Pjdk=java8

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
  options.compilerArgs.add("-XDenableSunApiLintControl")
}
