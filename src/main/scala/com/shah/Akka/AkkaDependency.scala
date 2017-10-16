package com.shah.Akka

/**
  * Parameters used in functions needing Akka's handling of future/synchronisation.
  */
trait AkkaDependency {
  import akka.actor.Scheduler
  implicit val scheduler: Scheduler

  import scala.concurrent.ExecutionContext
  implicit val ec: ExecutionContext
}
