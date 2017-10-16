package com.shah.Akka

import com.shah.util.Retry

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait AkkaRetry extends Retry{

  self: AkkaDependency ⇒

  import akka.pattern.after

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
