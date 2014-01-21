package com.cba.omnia.thermometer
package test

import com.twitter.scalding._

import org.specs2._
import org.specs2.matcher._


trait Spec extends Specification
  with TerminationMatchers
  with ThrownExpectations
  with ScalaCheck
