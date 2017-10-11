package com.shah

import spire.math.Numeric
import scala.concurrent.duration.FiniteDuration

package object util {

  object SeriesGenerators{

    def generateExponentialSeries[T](min: T, max: T, exponentialFactor: Double)
      (implicit num: Numeric[T]): Seq[T] = {

      assume(num.compare(min, num.zero) > 0, "min number should be positive")
      assume(num.compare(max, num.zero) > 0, "max number should be positive")
      assume(exponentialFactor > 1.0)

      val maxDouble = num.toDouble(max)

      import scala.annotation.tailrec
      @tailrec
      def accumulateSeries(acc: Seq[Double]): Seq[Double] = acc match {
        case head +: _ ⇒
          head * exponentialFactor match {
            case d if d <= maxDouble ⇒ accumulateSeries(d +: acc)
            case _ ⇒ maxDouble +: acc
          }
        case _ ⇒ acc
      }

      accumulateSeries(Seq(num.toDouble(min))).map(num.fromDouble).distinct.reverse
    }

    def generateExponentialTimeIntervals(
      minInterval: FiniteDuration,
      maxInterval: FiniteDuration,
      exponentialBackoffFactor: Double): Seq[FiniteDuration] = {

      assume(maxInterval > minInterval)

      val min: Long = minInterval.length
      val max: Long = maxInterval.toUnit(minInterval.unit).toLong

      val series = generateExponentialSeries(min, max, exponentialBackoffFactor)
      series.map(FiniteDuration(_,minInterval.unit))
    }

  }

}
