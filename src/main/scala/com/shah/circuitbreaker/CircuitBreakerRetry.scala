package com.shah.circuitbreaker

import com.shah.util.Retry

import scala.concurrent.duration.FiniteDuration

/**
  * [[CircuitBreakerRetry]] is a facade for a Circuit Breaker mechanism with the
  * added support for limited retries before finally returning a failure.
  **/

trait CircuitBreakerRetry {

  self: Retry ⇒

  import scala.concurrent.Future

  /**
    * The delay sequences attempted before finally giving up and returning failure.
    */
  val delaySeq: Seq[FiniteDuration]

  /**
    * Can be consulted to get an estimate of the total duration the retry mechanism tolerates before finally giving up.
    */
  def totalDelayTolerated: FiniteDuration

  /**
    * The function through which we depend on the underlying circuitBreaker implementation.
    * This function fails fast if the circuit breaker is not closed.
    */
  @inline
  private[circuitbreaker] def runWithCircuitBreaker[A](body: ⇒ Future[A]): Future[A]

  /**
    * This is the function the client uses for attempting a future call. In case of failure, it retries a few times
    * and finally gives up and returns the failure*/
  def runWithCircuitBreakerRetry[A](body: ⇒ Future[A]): Future[A] = {
    retry(runWithCircuitBreaker(body), delaySeq)
  }

}
