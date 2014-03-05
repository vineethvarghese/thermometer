package au.com.cba.omnia.thermometer
package tools

import cascading.flow.FlowDef
import cascading.pipe.Pipe
import au.com.cba.omnia.thermometer.fact._
import au.com.cba.omnia.thermometer.tools._
import au.com.cba.omnia.thermometer.context._
import com.twitter.scalding._
import com.twitter.scalding.typed.IterablePipe

import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.fs.FileStatus
import org.specs2._
import org.specs2.matcher._
import org.specs2.execute._
import org.specs2.specification.Fragments

import scalaz.{Failure => _, _}, Scalaz._
import scala.util.control.NonFatal

trait ScaldingSupport extends FieldConversions { self =>
  lazy val name: String =
    s"test-${java.util.UUID.randomUUID}"

  lazy val dir: String =
    s"/tmp/hadoop/${name}/mapred"

  lazy val conf: JobConf = new JobConf <| (conf => {
    new java.io.File(dir, "data").mkdirs()
    System.setProperty("user.dir", s"${dir}/user")
    conf.set("user.dir", s"${dir}/user")
    conf.set("jobclient.completion.poll.interval", "10")
    conf.set("cascading.flow.job.pollinginterval", "2")
    conf.set("mapred.local.dir", s"${dir}/local")
    conf.set("mapred.system.dir", s"${dir}/system")
    conf.set("hadoop.log.dir", s"${dir}/log")
    conf.set("fs.defaultFS", s"file://${dir}/data")
  })

  lazy val flowref =
    new java.util.concurrent.atomic.AtomicReference[FlowDef](new FlowDef <| (_.setName(name)))

  def resetFlow() =
    flowref.set(new FlowDef <| (_.setName(name)))

  implicit def flow =
    flowref.get

  implicit val mode: Mode =
    Hdfs(false, conf)

  lazy val scaldingArgs: Args =
    Mode.putMode(mode, Args("--hdfs"))

  implicit def PipeToRichPipe(pipe : Pipe): RichPipe =
    new RichPipe(pipe)
}
