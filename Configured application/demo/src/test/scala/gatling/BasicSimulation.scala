package gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {
  val url = System.getProperty("hello.test.service.uri")

  val scn = scenario("My scenario").repeat(3) {
    exec(
      http("Ping")
        .get(url)
        .check(status.is(200))
    ).pause(10 millisecond)
  }

  setUp(scn.inject(
    rampUsers(1000) during(20 seconds)
  )).assertions(global.successfulRequests.percent.is(100))
}
