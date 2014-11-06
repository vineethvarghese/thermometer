package au.com.cba.omnia.thermometer.example

import com.twitter.scalding.TypedPsv

import scalaz.effect.IO

import au.com.cba.omnia.thermometer.context.Context
import au.com.cba.omnia.thermometer.core._
import au.com.cba.omnia.thermometer.core.Thermometer._
import au.com.cba.omnia.thermometer.fact.PathFactoids.recordsByDirectory

class IntegrationSpec extends ThermometerSpec { def is = s2"""

Demonstration of testing output against files
=============================================

  Verify output using files            $facts
  Verify output using files2           $facts2

"""
  val data = List(
    Car("Canyonero", 1999),
    Car("Batmobile", 1966))

  val psvReader = ThermometerRecordReader[Car]((conf, path) => IO {
    new Context(conf).lines(path).map(line => {
      val parts = line.split('|')
      Car(parts(0), parts(1).toInt, parts(2))
    })
  })

  def pipeline = ThermometerSource[Car](data).map(c => (c.model, c.year, c.purchaseDate))
    .write(TypedPsv[(String, Int, String)]("output/cars/1"))
    .write(TypedPsv[(String, Int, String)]("output/cars/2"))

  val environment = path(getClass.getResource("env").toString)
  def facts = withEnvironment(environment)({
    pipeline
      .withFacts(
        path("output") ==> recordsByDirectory(psvReader, psvReader, path("expected"), (r: Car) => {
          r match { case Car(model, year, _) => Car(model, year, "DUMMY")}
        }))
  })
  
  val environment2 = path(getClass.getResource("env2").toString)
  def facts2 = withEnvironment(environment2)({
    pipeline
      .withFacts(
        path("output") ==> recordsByDirectory(psvReader, psvReader, path("expected2"), (r: Car) => {
          r match { case Car(model, year, _) => Car(model, year, "DUMMY")}
        }))
  })
}