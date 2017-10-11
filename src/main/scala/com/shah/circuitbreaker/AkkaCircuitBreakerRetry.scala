package com.shah.circuitbreaker

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import com.shah.util.SeriesGenerators._

trait AkkaCircuitBreakerRetryConfiguration{
  /** Maximum number of failures before opening the circuit */
  val maxFailures: Int

  /**Duration of time after which to consider a call a failure*/
  val callTimeout: FiniteDuration

  /** Duration of time after which to attempt to close the circuit
    * and also the smallest duration after which to attempt retries */
  val resetTimeout: FiniteDuration

  /**The circuitBreaker closing attempt timeOuts will grow with time and stay at a maximum value.
    * For the retry functionality however, that is the maximum wait amount tolerated, after which
    * the call fails.*/
  val maxResetTimeout: FiniteDuration

  /** The exponential factor by which the timeOut grows*/
  val exponentialBackoffFactor: Double

  import akka.actor.Scheduler
  implicit val scheduler: Scheduler

  import scala.concurrent.ExecutionContext
  implicit val ec: ExecutionContext
}


/**
  * [[AkkaCircuitBreakerRetry]] implements [[CircuitBreakerRetry]] by using Akka's circuit breaker internally.
  **/
trait AkkaCircuitBreakerRetry extends CircuitBreakerRetry
  with AkkaRetry with AkkaCircuitBreakerRetryConfiguration{

  import akka.pattern.CircuitBreaker

  private lazy val breaker = new CircuitBreaker(
    scheduler = scheduler,
    maxFailures = maxFailures,
    callTimeout = callTimeout,
    resetTimeout = resetTimeout,
    maxResetTimeout = maxResetTimeout,
    exponentialBackoffFactor = exponentialBackoffFactor
  )

  override lazy val delaySeq: Seq[FiniteDuration] = generateExponentialTimeIntervals(resetTimeout, maxResetTimeout, exponentialBackoffFactor)

  override lazy val totalDelayTolerated: FiniteDuration = delaySeq.fold(Duration.Zero)(_ + _ + callTimeout)

  override def runWithCircuitBreaker[A](body: ⇒ Future[A]): Future[A] = {
    breaker.withCircuitBreaker(body)
  }

}
