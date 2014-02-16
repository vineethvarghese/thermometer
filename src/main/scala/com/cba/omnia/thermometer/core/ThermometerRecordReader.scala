package com.cba.omnia.thermometer
package core

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import scalaz.effect.IO

case class ThermometerRecordReader[A](read: (Configuration, Path) => IO[List[A]])
