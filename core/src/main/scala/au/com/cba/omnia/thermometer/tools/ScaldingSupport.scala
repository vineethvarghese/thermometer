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

package au.com.cba.omnia.thermometer.tools

import java.util.concurrent.atomic.AtomicReference

import scalaz._, Scalaz._

import com.twitter.scalding._

import cascading.flow.FlowDef
import cascading.pipe.Pipe

/** Adds testing support for scalding by setting up a test `FlowDef` and `Mode`.*/
trait ScaldingSupport extends FieldConversions with HadoopSupport {
  /* Implicit conversion from Cascading Pipe to Scalding RichPipe.*/
  implicit def PipeToRichPipe(pipe : Pipe): RichPipe =
    new RichPipe(pipe)

  lazy val flowref =
    new AtomicReference[FlowDef](new FlowDef <| (_.setName(name)))

  def resetFlow() =
    flowref.set(new FlowDef <| (_.setName(name)))

  /** Test flow */
  implicit def flow: FlowDef =
    flowref.get

  /** Test mode */
  implicit val mode: Mode =
    Hdfs(false, jobConf)

  /** Default test Args*/
  lazy val scaldingArgs: Args =
    Mode.putMode(mode, Args("--hdfs"))

  /** Use `f` to create the job with the default args plus the specified arguments.*/
  def withArgs(args: Map[String, String])(f: Args => Job): Job = {
    f(args.foldLeft(scaldingArgs){ case (args, (key, value)) => args + (key -> List(value)) })
  }
}
