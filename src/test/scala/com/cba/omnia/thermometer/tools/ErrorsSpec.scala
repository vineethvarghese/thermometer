package com.cba.omnia.thermometer
package tools

import java.io._

class ErrorsSpec extends test.Spec { def is = s2"""

Errors Properties
=================

  render contains class name                $className
  render contains message                   $message

"""

  def className = prop((t: Throwable) => {
    val rendered = Errors.render(t)
    rendered.contains(t.getClass.getName)
  })

  def message = prop((message: String) => {
    val t = new Throwable(message)
    val rendered = Errors.render(t)
    rendered.contains(t.getMessage)
  })
}
