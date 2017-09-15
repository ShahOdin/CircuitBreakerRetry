package com.shah.circuitbreaker

import scala.concurrent.Future

/**
  * [[AkkaCircuitBreakerRetry]] implements [[CircuitBreakerRetry]] by using Akka's circuit breaker internally.
  **/
trait AkkaCircuitBreakerRetry extends CircuitBreakerRetry{

  /** Maximum number of failures before opening the circuit */
  val maxFailures: Int

  import scala.concurrent.duration.FiniteDuration

  /**Duration of time after which to consider a call a failure*/
  val callTimeout: FiniteDuration

  /** Duration of time after which to attempt to close the circuit
    * and also the smallest duration after which to attempt retries */
  val resetTimeout: FiniteDuration

  /**The circuitBreaker closing attempt timeOuts will grow with time and stay at a maximum value.
    * For the retry functionality however, that is the maximum wait amount tolerated, after which
    * the call fails.*/
  val maxResetTimeout: FiniteDuration

  import akka.pattern.CircuitBreaker

  /** The exponential factor by which the timeOut grows*/
  val exponentialBackoffFactor : Double

  import akka.actor.Scheduler
  implicit val scheduler: Scheduler

  import scala.concurrent.ExecutionContext
  implicit val ec: ExecutionContext

  private lazy val breaker = new CircuitBreaker(
    scheduler,
    maxFailures = maxFailures,
    callTimeout = callTimeout,
    resetTimeout = resetTimeout,
    maxResetTimeout = maxResetTimeout,
    exponentialBackoffFactor = exponentialBackoffFactor
  )

  import scala.annotation.tailrec
  @tailrec
  //TODO add random variation to the durations.
  private def constructDelaySeq(acum: Seq[FiniteDuration]): Seq[FiniteDuration] = acum match {
    case head +: _ ⇒
      head * exponentialBackoffFactor match {
        case f: FiniteDuration if f <= maxResetTimeout ⇒ constructDelaySeq(f +: acum)
        case _: FiniteDuration                         ⇒ maxResetTimeout +: acum
        case _                                         ⇒ acum
      }
    case _         ⇒ acum
  }

  //TODO: allow the user to customise the exceptions on which to attempt retry
  import akka.pattern.after

  import scala.concurrent.duration.Duration
  override def retry[T](body: => Future[T], delays: Seq[FiniteDuration]): Future[T] = {
    body recoverWith {
      case _ if delays.nonEmpty =>
        onRetry()
        after(delays.head, scheduler)(retry(body, delays.tail))
    }
  }

  override lazy val delaySeq = constructDelaySeq(Seq(resetTimeout)).reverse

  override lazy val totalDelayTolerated: FiniteDuration = delaySeq.fold(Duration.Zero)(_ + _ + callTimeout)

  override def runWithCircuitBreaker[A](body: ⇒ Future[A]): Future[A] = {
    breaker.withCircuitBreaker(body)
  }


}
