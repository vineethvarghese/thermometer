package au.com.cba.omnia.thermometer
package tools

import java.io._

class StreamsSpec extends test.Spec { def is = s2"""

Streams Properties
==================

  read all content                                $read
  write all content                               $write
  be symmetric in read and write                  $symmetric

"""

  def read = prop((s: String) => {
    val in = new ByteArrayInputStream(s.getBytes("UTF-8"))
    Streams.read(in, "UTF-8") must_== s
  })

  def write = prop((s: String) => {
    val out = new ByteArrayOutputStream()
    Streams.write(out, s)
    out.toByteArray must_== s.getBytes("UTF-8")
  })

  def symmetric = prop((s: String) => {
    val out = new ByteArrayOutputStream()
    Streams.write(out, s, "UTF-8")
    val in = new ByteArrayInputStream(out.toByteArray)
    Streams.read(in, "UTF-8") must_== s
  })
}
