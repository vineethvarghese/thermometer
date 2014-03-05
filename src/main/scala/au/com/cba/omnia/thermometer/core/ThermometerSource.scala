package au.com.cba.omnia.thermometer
package core

import cascading.flow.FlowDef
import com.twitter.scalding._
import com.twitter.scalding.typed.IterablePipe

object ThermometerSource {
  def apply[A](x: Seq[A])(implicit flow: FlowDef, mode: Mode): TypedPipe[A] =
    IterablePipe[A](x.toList, flow, mode)
}
