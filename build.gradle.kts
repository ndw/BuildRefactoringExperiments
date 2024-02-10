import com.saxonica.build.SaxonBuild

plugins {
  id("com.saxonica.build.saxon-build")
}

tasks.register("helloX") {
  doLast {
    println("Hello, world.")
      println(SaxonBuild.dateStamp)
  }
}
