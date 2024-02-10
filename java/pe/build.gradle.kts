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

// TODO: Exclude xml-apis as a transitive dependency

val peSourcesBucket by configurations.dependencyScope("peSourcesBucket")
val preprocessor by configurations.creating {
  extendsFrom(configurations["runtimeOnly"])
}

dependencies {
  peSourcesBucket(project(":java", "mainSourceElements"))

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

  preprocessor("com.igormaznitsa:jcp:7.0.4")
  preprocessor("com.tonicsystems:jarjar:0.6")
  preprocessor("net.sf.saxon:Saxon-HE:10.3")
  preprocessor(files(SaxonBuild.saxonLicenseDir))
}

val peSources = configurations.resolvable("peSources") {
  extendsFrom(peSourcesBucket)
}

val preprocessJava = tasks.register("preprocessJava") {
  inputs.files(peSources)
  outputs.dir(layout.buildDirectory.dir("src"))
  outputs.dir(layout.buildDirectory.dir("tmp/filtered"))

  doLast {
    copy {
      from(files(peSources)
               .asFileTree
               .matching {
                 include("net/sf/saxon/**/*.java")
                 include("javax/xml/xquery/*.java")
                 exclude("**/dotnet/**")
                 include("net/sf/saxon/**/package.html")
                 exclude("net/sf/saxon/testdriver/**")
                 exclude("net/sf/saxon/option/cpp/**")
                 include("com/saxonica/**/*.java")
                 exclude("com/saxonica/functions/extfn/cpp/*.java")
                 exclude("com/saxonica/testdriver/**")
                 exclude("com/saxonica/testdriver/ee/**/*.java")
                 exclude("com/saxonica/ee/**")
                 exclude("com/saxonica/config/EnterpriseConfiguration.java")
                 exclude("com/saxonica/expr/CastToUnion.java")
                 exclude("com/saxonica/expr/CastableToUnion.java")
                 exclude("com/saxonica/expr/DotNetExtensionFunctionCall.java")
                 exclude("com/saxonica/config/ee/**")
                 exclude("com/saxonica/js/**")
                 exclude("com/saxonica/config/DotNetExtensionLibrary.java")
                 exclude("com/saxonica/config/DotNetExtensionFunctionFactory.java")
                 exclude("com/saxonica/config/DotNetPlatformPE.java")
                 exclude("com/saxonica/config/JavaPlatformEE.java")
                 exclude("com/saxonica/config/NativePlatformPE.java")
                 exclude("com/saxonica/Validate.java")
                 exclude("com/saxonica/config/EnterpriseTransformerFactory.java")
                 exclude("com/saxonica/config/StreamingTransformerFactory.java")
                 exclude("com/saxonica/config/EnterpriseXPathFactory.java")
                 exclude("com/saxonica/config/DynamicLoaderEE.java")
                 exclude("com/saxonica/config/StandardSchemaResolver.java")
                 exclude("com/saxonica/jaxp/SchemaFactoryImpl11.java")
                 exclude("com/saxonica/functions/extfn/NativeCall.java")
               })
      into(layout.buildDirectory.dir("filtered"))
      filter { if (it.contains("import com.saxonica.ee")
                       || it.contains("import com.saxonica.config.ee")) "//" + it else it }
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
           "/i:${layout.buildDirectory.dir("filtered").get()}",
           "/o:${layout.buildDirectory.dir("src").get()}",
           "/p:EE=false", "/p:PE=true",
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
