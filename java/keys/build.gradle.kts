import kotlin.text.Charsets.UTF_8
import com.saxonica.build.SaxonBuild

plugins {
  id("com.saxonica.build.saxon-build")
}

repositories {
  mavenLocal()
  mavenCentral()
  maven { url = uri("https://maven.saxonica.com/maven") }
}

val transform by configurations.creating
val javaSources by configurations.creating

val featureKeys by configurations.creating {
  isCanBeConsumed = true
  isCanBeResolved = false
  extendsFrom(configurations["javaSources"])
}

dependencies {
  transform("net.sf.saxon:Saxon-HE:12.4")
  javaSources(files(layout.buildDirectory.dir("java")))
}

val featureKeysTask = tasks.register<JavaExec>("featureKeys") {
  inputs.dir(layout.projectDirectory.dir("../src/main/xml"))
  inputs.dir(layout.projectDirectory.dir("tools"))
  outputs.dir(layout.buildDirectory.dir("java"))
  classpath = configurations.named("transform").get()
  mainClass = "net.sf.saxon.Transform"
  args("${layout.projectDirectory.file("../src/main/xml/FeatureKeys.xml")}",
       "-xsl:${layout.projectDirectory.file("tools/FeatureKeysToJava.xsl")}",
       "-o:${layout.buildDirectory.file("java/net/sf/saxon/lib/establish-an-output-base-uri").get()}")

  doLast {
    delete("${layout.buildDirectory.file("java/net/sf/saxon/lib/establish-an-output-base-uri").get()}")
  }
}

// See: https://github.com/gradle/gradle/issues/27578
configurations.named("featureKeys") {
    outgoing
        .artifacts
        .find { it.name == "java" }
        ?.apply {
            this as ConfigurablePublishArtifact
            builtBy(featureKeysTask)
        }
}
