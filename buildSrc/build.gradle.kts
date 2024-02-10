plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
}

gradlePlugin {
  plugins {
    create("saxon-build") {
      id = "com.saxonica.build.saxon-build"
      implementationClass = "com.saxonica.build.SaxonBuild"
    }
  }
}
