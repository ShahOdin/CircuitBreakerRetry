package com.shah.circuitbreaker

import com.shah.Akka.AkkaDependency
import com.shah.util.Retry
import com.shah.util.SeriesGenerators._

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * [[AkkaCircuitBreakerRetry]] implements [[CircuitBreakerRetry]] by using Akka's circuit breaker internally.
  **/
trait AkkaCircuitBreakerRetry extends CircuitBreakerRetry {

  self: AkkaCircuitBreakerRetryConfiguration with AkkaDependency with Retry ⇒

  import akka.pattern.CircuitBreaker

  private lazy val breaker = new CircuitBreaker(
    scheduler = scheduler,
    maxFailures = maxFailures,
    callTimeout = callTimeout,
    resetTimeout = resetTimeout,
    maxResetTimeout = maxResetTimeout,
    exponentialBackoffFactor = exponentialBackoffFactor
  )(ec)

  override lazy val delaySeq: Seq[FiniteDuration] = generateExponentialTimeIntervals(resetTimeout, maxResetTimeout, exponentialBackoffFactor)

  override lazy val totalDelayTolerated: FiniteDuration = delaySeq.fold(Duration.Zero)(_ + _ + callTimeout)

  override def runWithCircuitBreaker[A](body: ⇒ Future[A]): Future[A] = {
    breaker.withCircuitBreaker(body)
  }

}
