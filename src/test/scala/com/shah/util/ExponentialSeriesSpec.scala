package com.shah.util

import spire.math.Numeric
import org.scalatest.FlatSpec

class ExponentialSeriesSpec extends FlatSpec {

  behavior of "A exponential series generation"

  private def assertExponentialSeriesCreation[T](min: T, max: T, exponentialFactor: Double, expectedLength: Int)
    (implicit num: Numeric[T]) = {
    val series = SeriesGenerators.generateExponentialSeries(min, max, exponentialFactor)
    println(series)
    assert(series.headOption.contains(min))
    assert(series.lastOption.contains(max))
    assert(series.length == expectedLength)
  }

  it should "handle Long values correctly" in {
    assertExponentialSeriesCreation(1, 12, 1.6, 6)
  }

  it should "handle Double values correctly" in {
    assertExponentialSeriesCreation(1.0, 12.0, 1.6, 7)
  }

  it should "not handle invalid inputs" in {
    //invalid exponential factor
    assertThrows[AssertionError](assertExponentialSeriesCreation(1, 12, 1, 7))

    //invalid ordering of min and max
    assertThrows[AssertionError](assertExponentialSeriesCreation(12, 1, 1, 7))

    //negative min or max values
    assertThrows[AssertionError](assertExponentialSeriesCreation(-1, 12, 1, 7))
    assertThrows[AssertionError](assertExponentialSeriesCreation(-10, -1, 1, 7))
  }

}
