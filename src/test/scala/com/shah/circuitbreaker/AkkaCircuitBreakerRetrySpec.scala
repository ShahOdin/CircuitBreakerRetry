package com.shah.circuitbreaker

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import org.scalatest.concurrent.ScalaFutures

class AkkaCircuitBreakerRetrySpec extends TestKit(ActorSystem("AkkaCircuitBreakerRetrySpec"))
  with ScalaFutures with WordSpecLike with Matchers with BeforeAndAfterEach
  with AkkaCircuitBreakerRetry {

  import akka.actor.Scheduler

  import scala.concurrent.ExecutionContext
  import scala.concurrent.duration.FiniteDuration

  override val scheduler: Scheduler = system.scheduler
  override val maxFailures: Int = 1

  import scala.concurrent.Future
  import scala.concurrent.duration._

  override val callTimeout: FiniteDuration = 2 seconds

  override val resetTimeout: FiniteDuration = 3 seconds
  override val maxResetTimeout: FiniteDuration = 8 seconds

  val exponentialBackoffFactor = 1.6

  override implicit val ec: ExecutionContext = system.dispatcher

  var reAttemptCount: Int = 0

  def resetAttemptCount() = {
    reAttemptCount = 0
  }

  override def onRetry() = {
    reAttemptCount += 1
    println(s"retry attempted for ${reAttemptCount}th time.")
    super.onRetry()
  }

  var delayFlag = false

  def potentiallyDelayedReturn(n: Int): Future[Int] = {
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

  override def beforeEach = {
    resetAttemptCount()
    delayFlag = false
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(callTimeout),
    interval = scaled(totalDelayTolerated + (delaySeq.length seconds) )
  )

  "A CircuitBreaker" must {

    "define delay seq based on the inputted resetTimeout and maxResetTimeout" in {
      println(s"delay Sequences: $delaySeq and total allowed delay: $totalDelayTolerated")
      delaySeq.length !== 0
    }

    "handle fast tasks immediately." in {
      runAsync {
        Future{1}
      }.futureValue shouldBe 1

      reAttemptCount shouldBe 0
    }

    "handle tolerable delays by retrying." in {
      runAsync {
        delayFlag = !delayFlag
        potentiallyDelayedReturn(3)
      }.futureValue shouldBe 3

      reAttemptCount shouldBe 1

    }

    "give up on intolerable delays after limited number of retry attempts." in {

      val intolerableDuration: Long = 40000

      intolerableDuration should be >= delaySeq.last.toMillis
      intolerableDuration should be >= patienceConfig.interval.toMillis

      runAsync {
        Future {
          Thread.sleep(intolerableDuration)
        }
      }.failed.futureValue shouldBe an[Exception]

      reAttemptCount shouldBe delaySeq.length

    }

  }
}
