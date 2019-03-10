plugins {
  // Apply the java plugin to add support for Java
  java
  // Apply the application plugin to add support for building an application
  application
}

repositories {
  jcenter()
  maven(url = "https://jitpack.io")
}

dependencies {
  // This dependency is found on compile classpath of this component and consumers
  implementation("com.github.abelsromero:java-parallels:master-SNAPSHOT")
  implementation("org.apache.httpcomponents:httpclient:4.5.7")
  implementation("com.google.code.gson:gson:2.8.5")
  // Use JUnit test framework
  testImplementation("junit:junit:4.12")
}

application {
  mainClassName = "edge.tester.Tester"
}
