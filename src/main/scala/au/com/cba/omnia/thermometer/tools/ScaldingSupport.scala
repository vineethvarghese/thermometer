//   Copyright 2014 Commonwealth Bank of Australia
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package au.com.cba.omnia.thermometer
package tools

import com.twitter.scalding._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapred.JobConf
import org.specs2._
import org.specs2.execute._
import org.specs2.matcher._

import scalaz.{Failure => _, _}, Scalaz._
import scalaz.effect.IO

import au.com.cba.omnia.thermometer.context._
import au.com.cba.omnia.thermometer.core._
import au.com.cba.omnia.thermometer.fact._
import au.com.cba.omnia.thermometer.tools._



import cascading.flow.FlowDef
import cascading.pipe.Pipe

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

  implicit def PathIterToThermometerRecordReader[A](toIter: (Configuration, Path) => Iterator[A]): ThermometerRecordReader[A] =
    new ThermometerRecordReader[A]((conf, path) => IO { toIter(conf, path).toList })
}
