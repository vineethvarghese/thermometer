package com.cba.omnia.thermometer.example

import com.cba.omnia.thermometer.core._, Thermometer._

import cascading.pipe.Pipe
import com.twitter.scalding._

class TypedPsvSpec extends ThermometerSpec { def is = s2"""

Demonstration of ThermometerSpec
================================

  Verify output using explicit expectations                 $expectations
  Verify output using fact api                              $facts

"""
  case class Car(model: String, year: Int) {
    def toPSV = s"${model}|${year}"
  }

  val data = List(
    Car("Canyonero", 1999),
    Car("Batmobile", 1966)
  )

  def pipeline =
    ThermometerSource[Car](data)
      .map(c => c.model -> c.year)
      .write(TypedPsv[(String, Int)]("cars"))

  def expectations =
    pipeline
      .withExpectations(t => {
        t.exists("cars" </> "_SUCCESS") must beTrue
        t.exists("cars" </> "part-00000") must beTrue
        t.lines("cars" </> "part-*") must contain(allOf(data.map(_.toPSV):_*))
      })

  import com.cba.omnia.thermometer.fact.PathFactoids._

  def facts =
    pipeline
      .withFacts(
        "cars" </> "_ERROR"      ==> missing
      , "cars" </> "_SUCCESS"    ==> exists
      , "cars" </> "part-00000"  ==> (exists, records(data.size))
      )
}
