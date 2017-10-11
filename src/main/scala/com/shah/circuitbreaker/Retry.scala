package com.shah.circuitbreaker

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
trait Retry {

  /**
    * could be used for logging retry attempts.
    */
  def onRetry(): Unit = ()

  /**
    * retries the future for as many times as we have delays configured.
    */
  def retry[T](body: => Future[T], delays: Seq[FiniteDuration]): Future[T]
}
