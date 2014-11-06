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

package au.com.cba.omnia.thermometer.example


import java.util.Date

import scalaz.effect.IO

import com.twitter.scalding._

import au.com.cba.omnia.thermometer.core._
import au.com.cba.omnia.thermometer.core.Thermometer._
import au.com.cba.omnia.thermometer.context.Context
import au.com.cba.omnia.thermometer.fact.PathFactoids._

class ExecutionSpec extends ThermometerSpec { def is = s2"""

Demonstration of ThermometerSpec using Execution monad
======================================================

  Verify output using explicit expectations $usingExpectations
  Verify output using fact api              $usingFacts
  Verify output against environment         $environment

"""
  val purchaseDate = new Date().toString

  val data = List(
    Car("Canyonero", 1999, purchaseDate),
    Car("Batmobile", 1966, purchaseDate)
  )

  def execution: Execution[Unit] =
    ThermometerSource[Car](data)
      .map(c => (c.model, c.year, c.purchaseDate))
      .writeExecution(TypedPsv[(String, Int, String)]("cars"))

  def usingExpectations = {
    executesOk(execution)

    expectations(t => {
      t.exists("cars" </> "_SUCCESS") must beTrue
      t.exists("cars" </> "part-00000") must beTrue
      t.lines("cars" </> "part-*") must contain(allOf(data.map(_.toPSV):_*))
    })
  }


  def usingFacts = {
    executesSuccessfully(execution) must_== (())

    facts(
      "cars" </> "_ERROR"      ==> missing
    , "cars" </> "_SUCCESS"    ==> exists
    , "cars" </> "part-00000"  ==> (exists, count(data.size))
    )
  }

  val psvReader = ThermometerRecordReader[Car]((conf, path) => IO {
    new Context(conf).lines(path).map(line => {
      val parts = line.split('|')
      Car(parts(0), parts(1).toInt, parts(2))
    })
  })

  def execution2: Execution[(Unit, Unit)] = {
    val pipe = ThermometerSource[Car](data).map(c =>  (c.model, c.year, c.purchaseDate))
    pipe.writeExecution(TypedPsv[(String, Int, String)]("output/cars/1"))
      .zip(pipe.writeExecution(TypedPsv[(String, Int, String)]("output/cars/2")))
  }

  def environment = withEnvironment(path(getClass.getResource("env").toString)) {
    executesOk(execution2)

    facts(
      path("output") ==> recordsByDirectory(psvReader, psvReader, path("expected"), (r: Car) => {
        r match { case Car(model, year, _) => model + year + "DUMMY"}
      })
    )
  }
}
