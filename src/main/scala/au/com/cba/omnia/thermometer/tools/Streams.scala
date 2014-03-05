package au.com.cba.omnia.thermometer
package tools

import java.io.{InputStream, OutputStream, ByteArrayOutputStream, PrintStream}

object Streams {
  val DefaultBufferSize = 4096

  def read(in: InputStream, encoding: String = "UTF-8", bufferSize: Int = DefaultBufferSize) = {
    val data = bytes(in, bufferSize)
    new String(data, encoding)
  }

  def bytes(in: InputStream, bufferSize: Int = DefaultBufferSize) = {
    val buffer = Array.ofDim[Byte](bufferSize)
    val out = new ByteArrayOutputStream
    var size = 0
    while ({ size = in.read(buffer); size != -1 })
      out.write(buffer, 0, size)
    out.toByteArray
  }

  def write(out: OutputStream, content: String, encoding: String = "UTF-8") = {
    val writer = new PrintStream(out, false, encoding);
    try writer.print(content)
    finally writer.close
  }
}
