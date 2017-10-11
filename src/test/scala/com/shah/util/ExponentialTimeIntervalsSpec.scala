package com.shah.util

import org.scalatest.FlatSpec

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

class ExponentialTimeIntervalsSpec extends FlatSpec{

  behavior of "A exponential series generation"

  private def assertIntervalsCreation(min: FiniteDuration, max: FiniteDuration, exponentialFactor: Double, expectedLength: Int) = {
    val series = SeriesGenerators.generateExponentialTimeIntervals(min, max, exponentialFactor)
    assert(series.headOption.contains(min))
    assert(series.lastOption.contains(max))
    assert(series.length == expectedLength)
  }

  it should "handle candid values correctly" in {
    assertIntervalsCreation(1 seconds , 12 seconds, 1.6, 6)
  }

  it should "not handle invalid inputs" in {
    assertThrows[AssertionError](assertIntervalsCreation(12 seconds, 1 seconds, 1, 7))
  }

}
