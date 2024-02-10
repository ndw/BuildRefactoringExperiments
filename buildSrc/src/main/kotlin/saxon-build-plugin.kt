package com.saxonica.build

import org.gradle.api.artifacts.Configuration
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.internal.os.OperatingSystem;
import org.gradle.kotlin.dsl.create
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

interface SaxonBuildExtension {
  abstract val project: Property<Project>
}

class SaxonBuild : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create<SaxonBuildExtension>("saxonbuild")
    extension.project.set(project)
  }

  companion object {
    val dateStamp = SimpleDateFormat("yyyyMMdd").format(Date()).toString()
    val pathSeparator = System.getProperty("path.separator")
    val fileSeparator = System.getProperty("file.separator")
    val systemOs = OperatingSystem.current()
    val systemOsName = if (systemOs.isMacOsX())
      "mac" else if (systemOs.isLinux())
        "linux" else if (systemOs.isWindows())
          "windows" else "unknown"
    val arch = System.getProperty("os.arch")
    val systemOsArch = if (arch == "amd64") "x86_64" else arch
    val dotnetex = findExecutable("dotnet", listOf("/usr/local/share/dotnet", "/usr/local/share/dotnet/x64"))
    val dotnetversion = "6.0"
    val saxonLicenseDir = System.getenv("SAXON_LICENSE_DIR")
        ?: System.getenv("HOME") + "/java"
    val signNugetTool = "/Users/norm/bin/nuget.exe"
    val canSignNuget = systemOs.isWindows() && File(signNugetTool).exists()

    fun unexpectedReleaseName(name: String) {
      throw GradleException("Unexpected release: ${name}")
    }

    fun findJar(configuration: Configuration, jarname: String): String {
      val jar = configuration.getFiles()
          .filter { jar -> jar.toString().endsWith(jarname) }
          .elementAtOrNull(0)
      if (jar == null) {
        throw GradleException("Cannot find ${jarname} in configuration")
      }
      return jar.toString()
    }

    fun findExecutable(executable: String, extraPaths: List<String> = emptyList()): String? {
      val spath = extraPaths + System.getenv("PATH").split(pathSeparator)
      var found: String? = null
      spath.forEach { path ->
        if (found == null) {
          val fn = File(path + fileSeparator + executable)
          if (fn.exists() && fn.canExecute()) {
            found = fn.toString()
          } else {
            val wfn = File(path + fileSeparator + executable + ".exe")
            if (wfn.exists() && wfn.canExecute()) {
              found = wfn.toString()
            }
          }
        }
      }

      return found
    }
    
/*
  signAlias = findProperty("saxonSignAlias") ?: System.getenv("SAXON_SIGN_ALIAS")
  signPassword = findProperty("saxonSignPassword") ?: System.getenv("SAXON_SIGN_PASSWORD")
  signKeystore = findProperty("saxonSignKeystore") ?: System.getenv("SAXON_SIGN_KEYSTORE")

  signExe = { exe ->
    def signExeTool = (systemOs.isMacOsX()
                       ? findProperty("macSignTool") ?: System.getenv("MAC_SIGN_TOOL")
                       : (systemOs.isWindows()
                          ? findProperty("windowsSignTool") ?: System.getenv("WINDOWS_SIGN_TOOL")
                          : null))

    def signExeOptions = (systemOs.isMacOsX()
                          ? ['-s', 'SAXONICA LIMITED']
                          : (systemOs.isWindows()
                             ? ["sign", "/fd", "sha1",
                                "/f", signKeystore,
                                "/t", "http://timestamp.digicert.com",
                                "/p", signPassword]
                             : []))

    if (signExeTool == null) {
      println("Cannot sign executables on ${systemOsName} systems")
    } else {
      println("Signing ${exe}")
      def args = [signExeTool] + signExeOptions + [exe]
      exec {
        commandLine args
      }
    }
  }
*/

/*
    fun signNuget(pkg: String, outputdir: String)
    // Hack. This only works on the release machine, wynngifu
    def signNugetOptions = ["sign", pkg,
                            "-CertificatePath", "C:\\Users\\norm\\java\\saxonica_certificate.pfx",
                            "-Timestamper", "http://timestamp.digicert.com",
                            "-OutputDirectory", outputdir,
                            "-Verbosity", "detailed", "-CertificatePassword",
                            project.findProperty("saxonSignPassword")]

    if (canSignNuget) {
      println("Signing ${pkg}")
      def args = [signNugetTool] + signNugetOptions
      exec {
        commandLine args
      }
    } else {
      println("Cannot sign NuGet packages on this system")
    }
  }
}
*/

  }
}
