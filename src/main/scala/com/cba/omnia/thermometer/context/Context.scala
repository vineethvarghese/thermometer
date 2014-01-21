package com.cba.omnia.thermometer
package context

import com.cba.omnia.thermometer.tools._
import com.cba.omnia.thermometer.core.Thermometer._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path, FileStatus}
import org.specs2.execute._

import java.io.{InputStreamReader, BufferedReader}

import scala.util.control.NonFatal
import scala.collection.JavaConverters


/**
 * A `Context` is a HDFS wrapper that sacrifices all safety, composability
 * and sanity in order to give the illusion that you may be able to test a
 * scalding job. Methods on thermometer return raw values that are appropriate
 * to use with specs2 matchers.
 */
case class Context(config: Configuration) {
  def withConfig[A](f: Configuration => A)(message: String): A =
    try f(config)
    catch {
      case NonFatal(t) =>
        val detail =
          """This test is failing because an error has occurred trying to perform an HDFS operation.
            |
            |This usually (but not always) indicates the scalding code has run to completion, but
            |there is an issue within the assertions being made.
            |
            |${Errors.renderWithStack(t)}"""
        throw new FailureException(Failure(message, detail, t.getStackTrace.toList))
    }

  def withFileSystem[A](f: FileSystem => A)(message: String): A =
    withConfig(config => f(FileSystem.get(config)))(message)

  def withFiles[A](paths: List[Path], f: (FileSystem, Path) => A)(message: String): List[A] =
    withFileSystem(system =>
      paths.flatMap(pattern =>
        glob(pattern).map(file =>
          f(system, file))))(message)

  def exists(paths: Path*): Boolean =
    withFileSystem(system =>
      paths.forall(system.exists)
    )(s"""exists(${paths.mkString(", ")})""")

  def stat(path: String): FileStatus =
    stat(path.toPath)

  def stat(path: Path): FileStatus =
    withFileSystem(system =>
      system.getFileStatus(path)
    )(s"""stat(${path})""")

  def stats(paths: Path*): List[FileStatus] =
    paths.toList.map(stat)

  def lines(paths: Path*): List[String] =
    withFiles(paths.toList, (system, file) => {
      val in = system.open(file)
      try Streams.read(in).lines.toList
      finally in.close
    })(s"""lines(${paths.mkString(", ")})""").flatten

  def withLines[A](paths: Path*)(z: A)(f: (A, String) => A): A =
    withFileSystem(system => {
      var state = z
      paths.flatMap(pattern =>
        glob(pattern).map(file => {
          val in = system.open(file)
          try {
            val reader = new BufferedReader(new InputStreamReader(in))
            var line = ""
            while ({line = reader.readLine; line != null})
              state = f(state, line)
          } finally in.close}))
       state
    })(s"""withLines(${paths.mkString(", ")})""")

  def forLines(paths: Path*)(f: String => Unit): Unit =
    withLines[Unit](paths:_*)(())((_, line) => f(line))

  def content(paths: Path*): String =
    withFiles(paths.toList, (system, file) => {
      val in = system.open(file)
      try Streams.read(in)
      finally in.close
    })(s"""content(${paths.mkString(", ")})""").mkString("\n")

  def glob(pattern: Path): List[Path] =
    withFileSystem(system =>
      system.globStatus(pattern).toList.map(_.getPath)
    )(s"""glob(${pattern})""")
}
