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
import au.com.cba.omnia.thermometer.core._

import org.apache.hadoop.fs.Path
import org.specs2._
import org.specs2.execute._
import org.specs2.matcher._

import scalaz._, Scalaz._

case class PathFact(path: Path) {
  def apply(factoid: PathFactoid*): Fact =
    Fact(thermometer =>
      factoid.toList.map(f => f.run(thermometer, path)).suml(Result.ResultMonoid))
}

case class PathFactoid(run: (Context, Path) => Result)

object PathFactoids extends ThrownExpectations {
  def conditional(cond: (Context, Path) => Boolean)(message: Path => String): PathFactoid =
    PathFactoid((context, path) => if (cond(context, path)) ok.toResult else failure(message(path)))

  def exists: PathFactoid =
    conditional(_.exists(_))(path => s"Path <${path}> does not exist when it should.")

  def missing: PathFactoid =
    conditional(!_.exists(_))(path => s"Path <${path}> exists when it should not.")

  def count(n: Int): PathFactoid =
    PathFactoid((context, path) => {
      val count = context.lines(path).size
      if (count == n) ok.toResult else failure(s"Path <${path}> exists but it contains ${count} records where we expected ${n}.") })

  def records[A](reader: ThermometerRecordReader[A], data: List[A]): PathFactoid =
    PathFactoid((context, path) => {
      val records = context.glob(path).flatMap(p =>
        reader.read(context.config, p).unsafePerformIO)
      if (records.toSet == data.toSet)
        ok.toResult
      else
        failure(s"""Path <${path}> exists but it contains records that don't match. Expected [${data.mkString(", ")}], got [${records.mkString(", ")}].""") })

  def recordCount[A](reader: ThermometerRecordReader[A], n: Int): PathFactoid =
    PathFactoid((context, path) => {
      val records = context.glob(path).flatMap(p =>
        reader.read(context.config, p).unsafePerformIO)
      if (records.size == n)
        ok.toResult
      else
        failure(s"""Path <${path}> exists but it contains ${records.size} records and we expected ${n}.""") })

  def lines(expected: List[String]): PathFactoid =
    PathFactoid((context, path) => {
      val actual = context.lines(context.glob(path):_*)
      if (actual.toSet == expected.toSet)
        ok.toResult
      else
        failure(s"""Path <${path}> exists but it contains records that don't match. Expected [${expected.mkString(", ")}], got [${actual.mkString(", ")}].""") })

}
