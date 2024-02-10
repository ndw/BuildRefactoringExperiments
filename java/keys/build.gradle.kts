import kotlin.text.Charsets.UTF_8
import com.saxonica.build.SaxonBuild

plugins {
  id("java-library")
  id("com.saxonica.build.saxon-build")
}

repositories {
  mavenLocal()
  mavenCentral()
  maven { url = uri("https://maven.saxonica.com/maven") }
}

val transform by configurations.creating

dependencies {
  transform("net.sf.saxon:Saxon-HE:12.4")
}

val featureKeys = tasks.register<JavaExec>("featureKeys") {
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

sourceSets {
  main {
    java {
      srcDir(featureKeys)
    }
  }
}
