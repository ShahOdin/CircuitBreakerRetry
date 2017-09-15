# CircuitBreakerRetry

[CircuitBreaker](https://en.wikipedia.org/wiki/Circuit_breaker_design_pattern) is a design pattern used in modern software development. It is used to detect failures and encapsulates the logic of preventing a failure from constantly recurring, during maintenance, temporary external system failure or unexpected system difficulties. The following diagram demonstrates the basic idea behind the pattern:

![circuit-breaker implementation](http://doc.akka.io/docs/akka/2.5/images/circuit-breaker-states.png)

Akka offers an [implementation](http://doc.akka.io/docs/akka/2.5/scala/common/circuitbreaker.html) which can be used in projects based in Akka or Play.

This project aims to add a retry functionality on top of it, so that instead of calls falling fast, they would be retried with exponentially growing intervals for a limited number of times before finally returning a failure. This would allow the user to tolerate negligible downtimes for the server providing a functionality.

The behaviour is demonstrated in the spec [file](https://github.com/ShahOdin/CircuitBreakerRetry/blob/master/src/test/scala/com/shah/circuitbreaker/AkkaCircuitBreakerRetrySpec.scala).

Note that the main idea behind the retry mechanism is abstracted out in [CircuitBreakerRetry](https://github.com/ShahOdin/CircuitBreakerRetry/blob/master/src/main/scala/com/shah/circuitbreaker/CircuitBreakerRetry.scala) while the interface with Akka is provided in [AkkaCircuitBreakerRetry](https://github.com/ShahOdin/CircuitBreakerRetry/blob/master/src/main/scala/com/shah/circuitbreaker/AkkaCircuitBreakerRetry.scala), so the ideas can be easily applied to internally use another circuitBreaker provider such as Amazon Hystrix.