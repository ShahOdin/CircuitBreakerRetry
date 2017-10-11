package com.shah.circuitbreaker

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait AkkaRetry extends Retry{

  import akka.pattern.after

  import akka.actor.Scheduler
  implicit val scheduler: Scheduler

  import scala.concurrent.ExecutionContext
  implicit val ec: ExecutionContext

  /**
    * uses Akka's scheduler to retry task at specified intervals.
    **/
  override def retry[T](body: => Future[T], delays: Seq[FiniteDuration]): Future[T] = {
    body recoverWith {
      case _ if delays.nonEmpty =>
        onRetry()
        after(delays.head, scheduler)(retry(body, delays.tail))
    }
  }

}
