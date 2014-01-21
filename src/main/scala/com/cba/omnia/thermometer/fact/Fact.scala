package com.cba.omnia.thermometer
package fact

import com.cba.omnia.thermometer.context._

import org.apache.hadoop.fs.Path
import org.specs2._
import org.specs2.execute._
import org.specs2.matcher._

import scalaz._, Scalaz._

case class Fact(run: Context => Result)

object Fact {
  def build[A: AsResult](run: Context => A) =
    Fact(t => implicitly[AsResult[A]].asResult(run(t)))

  implicit def FactMonoid: Monoid[Fact] =
    new Monoid[Fact] {
      import Result.ResultMonoid

      def zero =
        Fact.build(_ => true)

      def append(a: Fact, b: => Fact) =
        Fact(t =>
          a.run(t) |+| b.run(t))
    }
}
