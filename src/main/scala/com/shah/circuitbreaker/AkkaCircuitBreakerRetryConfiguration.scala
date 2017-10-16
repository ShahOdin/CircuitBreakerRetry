package com.shah.circuitbreaker

import scala.concurrent.duration.FiniteDuration

/**
  * to be shared across circuit Breaker implementations.
  **/
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
}
