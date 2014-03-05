package au.com.cba.omnia.thermometer
package tools

import cascading.flow.FlowDef
import com.twitter.scalding._
import scala.util.control.NonFatal
import scalaz._, Scalaz._

object Jobs {
  /** Run the specified job, returns an optional error state (None indicates success). */
  def runJob(job: Job): Option[String \/ Throwable] = {
    def start(j : Job, cnt : Int): Option[String \/ Throwable] = try {
      val successful = {
        j.validate
        j.run
      }
      j.clear

      if(successful)
        j.next.flatMap(nextj => start(nextj, cnt + 1))
      else
        Some(s"Job failed to run <${j.name}>".left)
    } catch {
      case NonFatal(e) => Some(e.right)
    }

    start(job, 0)
  }
}
