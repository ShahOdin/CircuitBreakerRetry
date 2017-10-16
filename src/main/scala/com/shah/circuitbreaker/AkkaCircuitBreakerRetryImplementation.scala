package com.shah.circuitbreaker

import com.shah.Akka.{AkkaDependency, AkkaRetry}

trait AkkaCircuitBreakerRetryImplementation extends AkkaCircuitBreakerRetry with AkkaRetry{

  self: AkkaCircuitBreakerRetryConfiguration with AkkaDependency ⇒

}
