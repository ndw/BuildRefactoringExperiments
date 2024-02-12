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

// TODO: Exclude xml-apis as a transitive dependency

val heSourcesBucket by configurations.dependencyScope("heSourcesBucket")
val featureKeysBucket by configurations.dependencyScope("featureKeysBucket")

val preprocessor by configurations.creating {
  extendsFrom(configurations["runtimeOnly"])
}

dependencies {
  heSourcesBucket(project(":java", "mainSourceElements"))

  featureKeysBucket(project(mapOf("path" to ":java:keys",
                                  "configuration" to "featureKeys")))

  compileOnly("org.apache.ws.commons.axiom:axiom:1.2.15")
  compileOnly("org.apache.ws.commons.axiom:axiom-dom:1.2.15")
  compileOnly("org.apache.ws.commons.axiom:axiom-impl:1.2.15")
  compileOnly("com.ibm.icu:icu4j:${findProperty("icu4jVersion")}")
  compileOnly("jline:jline:${findProperty("jlineVersion")}")
  compileOnly("org.xmlresolver:xmlresolver:${findProperty("xmlresolverVersion")}")
  compileOnly("org.xmlresolver:xmlresolver:${findProperty("xmlresolverVersion")}:data")
  compileOnly("xom:xom:1.3.5")
  compileOnly("org.jdom:jdom:1.1.3")
  compileOnly("org.jdom:jdom2:2.0.6.1")
  compileOnly("dom4j:dom4j:1.6.1")

  preprocessor(project(":java:keys"))
  preprocessor("com.igormaznitsa:jcp:7.0.4")
  preprocessor("com.tonicsystems:jarjar:0.6")
  preprocessor("net.sf.saxon:Saxon-HE:12.4")
  preprocessor(files(SaxonBuild.saxonLicenseDir))
}

val heSources = configurations.resolvable("heSources") {
  extendsFrom(heSourcesBucket)
}

val featureKeysSources = configurations.resolvable("featureKeysSources") {
  extendsFrom(featureKeysBucket)
}

val preprocessJava = tasks.register("preprocessJava") {
  inputs.files(heSources)
  inputs.files(featureKeysSources)
  outputs.dir(layout.buildDirectory.dir("src"))

  doLast {
    copy {
      from(files(heSources)
               .asFileTree
               .matching {
                 include("net/sf/saxon/**/*.java")
                 include("net/sf/saxon/**/package.html")
                 exclude("javax/xml/xquery/*.java")
                 exclude("**/dotnet/**")
                 exclude("net/sf/saxon/testdriver/**")
                 exclude("net/sf/saxon/option/sql/**")
                 exclude("net/sf/saxon/option/cpp/**")
               })
      into(layout.buildDirectory.dir("tmp/filtered"))
      filter { if (it.contains("import com.saxonica")) "//" + it else it }
    }

    copy {
      from(files(featureKeysSources))
      into(layout.buildDirectory.dir("tmp/filtered"))
    }
  }

  doFirst {
    mkdir(layout.buildDirectory.dir("tmp/filtered"))
  }

  doLast {
    javaexec {
      classpath = configurations.named("preprocessor").get()
      mainClass = "com.igormaznitsa.jcp.JcpPreprocessor"
      args("/c",
           "/i:${layout.buildDirectory.dir("tmp/filtered").get()}",
           "/o:${layout.buildDirectory.dir("src").get()}",
           "/p:EE=false", "/p:PE=false",
           "/p:OPT=false", "/p:SAXONC=false", "/p:BYTECODE=false",
           "/p:CSHARP=false")
    }
  }
}

sourceSets {
  main {
    java {
      srcDir(preprocessJava)
    }
  }
}

tasks.register("helloWorld") {
  doLast {
    println("Hello, world.")
    files(featureKeysSources).asFileTree.map {
      println("F: ${it}")
    }
  }
}
