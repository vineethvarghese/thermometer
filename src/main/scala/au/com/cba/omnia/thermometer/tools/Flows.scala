package au.com.cba.omnia.thermometer
package tools

import cascading.flow.FlowDef
import com.twitter.scalding._
import scala.util.control.NonFatal
import scalaz._, Scalaz._

object Flows {
  /** Run the current test FlowDef, returns an optional error state (None indicates success). */
  def runFlow(scaldingArgs: Args, scaldingFlow: FlowDef, scaldingMode: Mode): Option[String \/ Throwable] = {
    val job = new Job(scaldingArgs) {
      override val flowDef: FlowDef = scaldingFlow
      override def mode: Mode = scaldingMode
    }
    Jobs.runJob(job)
  }
}
