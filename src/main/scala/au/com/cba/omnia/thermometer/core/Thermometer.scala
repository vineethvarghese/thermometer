package au.com.cba.omnia.thermometer
package core

import au.com.cba.omnia.thermometer.fact._
import org.apache.hadoop.fs.Path

object Thermometer {
  def path(s: String): Path =
    new Path(s)

  def paths(ss: List[String]): List[Path] =
    ss.map(path)

  implicit class ThermometerStringSyntax(s: String) {
    def toPath: Path = path(s)
    def </>(path: String): Path = new Path(s, path)
    def </>(path: Path): Path = new Path(s, path)
    def ==> : PathFact = PathFact(toPath)
  }

  implicit class ThermometerPathSyntax(p: Path) {
    def </>(path: String): Path = new Path(p, path)
    def </>(path: Path): Path = new Path(p, path)
    def ==> : PathFact = PathFact(p)
  }
}
