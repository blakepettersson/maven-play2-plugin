package de.akquinet.innovation.play.util

import java.io.File
import org.apache.maven.plugin.MojoFailureException
import scalax.file.Path
import scalax.file.PathMatcher.IsFile

/**
 *
 * @author blake
 *
 */
object Test {
  private val defaultImports = List("play.api.templates._", "play.api.templates.PlayMagic._")
  private val scalaImports = List(
    "models._",
    "controllers._",
    "play.api.i18n._",
    "play.api.mvc._",
    "play.api.data._",
    "views.%format%._")
  private val javaImports = List(
    "models._",
    "controllers._",
    "java.lang._",
    "java.util._",
    "scala.collection.JavaConversions._",
    "scala.collection.JavaConverters._",
    "play.api.i18n._",
    "play.core.j.PlayMagicForJava._",
    "play.mvc._",
    "play.data._",
    "play.api.data.Field",
    "play.mvc.Http.Context.Implicit._",
    "views.%format%._")

  def compileRoutes(baseDirectory:File, targetDirectory:File, language:String) = {
    import play.router.RoutesCompiler._

    val srcManaged = Path(baseDirectory) / Path("app") / Path("src_managed")
    srcManaged.createDirectory(failIfExists = false, createParents = true)

    srcManaged.descendants(IsFile)
      .filter(f => f.name.endsWith("routes.java") || f.name.contains("routes") && f.name.endsWith(".scala"))
      .foreach(f => GeneratedSource(f.jfile).sync())

    try {
      val routeSources = Path(baseDirectory) / Path("conf") descendants(IsFile) filter(_.name.endsWith("routes"))

      println(routeSources.size + " routing file(s) found....")

      val imports = if (language == "scala") Nil else Seq("play.libs.F")
      routeSources.foreach(routesFile => compile(routesFile.jfile, srcManaged.jfile, imports))
    } catch {
      case e:RoutesCompilationError => throw new MojoFailureException("Failed to compile routes",e)
        //throw reportCompilationError(state, RoutesCompilationException(source, message, line, column.map(_ - 1)))
    }
  }

  def compileTemplates(sourceDirectory:File, targetDirectory:File, language:String) = {
    import play.templates._
    import play.templates.ScalaTemplateCompiler._

    val templateExt: PartialFunction[File, (File, String, String, String)] = {
      case p if templateTypes.isDefinedAt(p.getName.split('.').last) =>
        val extension = p.getName.split('.').last
        val exts = templateTypes(extension)
        (p, extension, exts._1, exts._2)
    }

    val srcManaged = Path(sourceDirectory) / Path("src_managed")
    srcManaged.createDirectory(failIfExists = false, createParents = true)

    try {
      val files = Path(sourceDirectory).descendants(IsFile).filter(_.name.contains(".scala.")).toList

      println(files.size + " template(s) found....")
      files.foreach(f => println(f.path))

      val imports = defaultImports ::: (if(language == "scala") scalaImports else javaImports)

      files.map(_.jfile).collect(templateExt).foreach {
        case (template, extension, t, format) =>
          compile(
            template,
            sourceDirectory,
            srcManaged.jfile,
            t,
            format,
            imports.map("import " + _.replace("%format%", extension)).mkString("\n")
          )
      }
    } catch {
      case e:TemplateCompilationError => {
        throw new MojoFailureException("Failed to compile template(s)",e)
        //reportCompilationError(state, TemplateCompilationException(source, message, line, column - 1))
      }
    }

  }

  private def templateTypes:PartialFunction[String, (String, String)] = {
    case "html" => ("play.api.templates.Html", "play.api.templates.HtmlFormat")
    case "txt" => ("play.api.templates.Txt", "play.api.templates.TxtFormat")
    case "xml" => ("play.api.templates.Xml", "play.api.templates.XmlFormat")
  }
}
