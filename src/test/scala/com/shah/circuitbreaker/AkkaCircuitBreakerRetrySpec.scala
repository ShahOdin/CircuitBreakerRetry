package com.shah.circuitbreaker

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.shah.Akka.AkkaDependency
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.{ExecutionContext, Future}

class AkkaCircuitBreakerRetrySpec extends TestKit(ActorSystem("AkkaCircuitBreakerRetrySpec"))
  with ScalaFutures with WordSpecLike with Matchers with BeforeAndAfterEach {

  trait CircuitBreakerParameters extends AkkaDependency with AkkaCircuitBreakerRetryConfiguration {
    import akka.actor.Scheduler
    import scala.concurrent.ExecutionContext

    import scala.concurrent.duration.FiniteDuration

    override val scheduler: Scheduler = system.scheduler
    override implicit val ec: ExecutionContext = system.dispatcher

    override val maxFailures: Int = 1

    import scala.concurrent.duration._

    override val callTimeout: FiniteDuration = 100.millis
    override val resetTimeout: FiniteDuration = 200.millis
    override val maxResetTimeout: FiniteDuration = 400.millis

    val exponentialBackoffFactor = 1.6

  }

  object CircuitBreaker extends AkkaCircuitBreakerRetryImplementation with CircuitBreakerParameters {
    override def onRetry(): Unit = {
      reAttemptCount += 1
      println(s"retry attempted for ${reAttemptCount}th time.")
      super.onRetry()
    }
  }

  var reAttemptCount: Int = 0

  private def resetAttemptCount() = {
    reAttemptCount = 0
  }

  var delayFlag = false

  implicit val ec: ExecutionContext = system.dispatcher

  def potentiallyDelayedReturn(n: Int)(implicit ec: ExecutionContext): Future[Int] = {
    Future {
      if (!delayFlag) {
        println("returning fast")
        n
      } else {
        println(s"delayed return. going to sleep for $n seconds")
        Thread.sleep(n * 1000)
        n
      }
    }
  }

  override def beforeEach: Unit = {
    resetAttemptCount()
    delayFlag = false
  }

  import scala.concurrent.duration._
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(CircuitBreaker.callTimeout),
    interval = scaled(CircuitBreaker.totalDelayTolerated + CircuitBreaker.delaySeq.length.seconds)
  )

  "A CircuitBreaker" must {

    "define delay seq based on the inputted resetTimeout and maxResetTimeout" in {
      println(s"delay Sequences: ${CircuitBreaker.delaySeq} and total allowed delay: ${CircuitBreaker.totalDelayTolerated}")
      CircuitBreaker.delaySeq.length !== 0
    }

    "handle fast tasks immediately." in {
      CircuitBreaker.runWithCircuitBreakerRetry {
        Future {
          1
        }
      }.futureValue shouldBe 1

      reAttemptCount shouldBe 0
    }

    "handle tolerable delays by retrying." in {
      CircuitBreaker.runWithCircuitBreakerRetry {
        delayFlag = !delayFlag
        potentiallyDelayedReturn(3)
      }.futureValue shouldBe 3

      reAttemptCount shouldBe 1

    }

    "give up on intolerable delays after limited number of retry attempts." in {

      val intolerableDuration: Long = 5000

      intolerableDuration should be >= CircuitBreaker.delaySeq.last.toMillis
      intolerableDuration should be >= patienceConfig.interval.toMillis

      CircuitBreaker.runWithCircuitBreakerRetry {
        Future {
          Thread.sleep(intolerableDuration)
        }
      }.failed.futureValue shouldBe an[Exception]

      reAttemptCount shouldBe CircuitBreaker.delaySeq.length

    }

  }
}
