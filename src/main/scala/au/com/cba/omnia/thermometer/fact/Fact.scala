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
package fact

import au.com.cba.omnia.thermometer.context._

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
