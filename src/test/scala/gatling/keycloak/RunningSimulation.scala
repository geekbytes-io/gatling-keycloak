package gatling.keycloak

import java.util.UUID

import com.typesafe.config.Config
import gatling.keycloak.admin.{ClientAccess, RealmAccess, ResourceAccess, UserAccess}
import io.gatling.commons.validation.Success
import io.gatling.core.session.Expression
import io.gatling.core.structure.{PopulationBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.core.pause.NormalWithPercentageDuration

import scala.concurrent.duration._
import scala.util.Random

/*
* Copyright (c) 2017 Call Handling Services [http://www.callhandling.co.uk]
* Created by mts.manu
* 11/7/2017
*/

/*
class EnableAuth extends KeycloakSimulation with TokenAccess with ClientAccess with ResourceAccess {


  //setUp(Range(0, 50).map(i => (setInfrastructure(i, i).inject(atOnceUsers(1))).protocols(httpProtocol)): _*)
  //setUp(execBatch("failed_201To400", List(342,292,262,352,312,202,272,232,214,345,253,323,354,364,274,316)).inject(atOnceUsers(1)).protocols(httpProtocol))

  setUp(scenario(s"enableAuth")
    .exec(getAdminToken())
    .exec(
      List(303, 311, 313, 314, 315, 321, 324, 325, 331, 332, 335, 336, 337, 338, 341, 343, 344, 346, 347, 348, 351, 353, 357, 358, 361, 363, 365, 366, 371, 381, 383, 391, 393, 394, 395).map(
        realmIndex => {
          enableAuthAndAddResource(s"try_test_realm_${realmIndex.toString}")
        }
      ))
    .inject(atOnceUsers(1)).protocols(httpProtocol))

  /*
  set master client
  setUp(
    scenario("set_master_client").exec(
      exec(getAdminToken()).exec(setClient("master", "test_client")).exec(setUser("master", "test_user")).exec(updatePassword("master", "test_user", "test_user"))
    ).inject(atOnceUsers(1)).protocols(httpProtocol)
  )*/

  private def enableAuthAndAddResource(realm: String) = {
    exec(getClientId(realm, "test_client"))
      .doIf(session => !session.contains(s"${realm}_clientId")) {
        exec(createClient(realm, "test_client"))
      }
      .exec(enableAuthorizationServicesOnClient(realm, "test_client"))
      .exec(createResource(realm, "test_client")("test_resource", "test", "test:test", List("view", "edit")))
  }
}*/

class Options(config: Config) {
  val refreshTokenProbability = config.getInt("test.refreshTokenProbability")
  val refreshTokenPeriod = config.getInt("test.refreshTokenPeriod")

  val usersPerSecond = config.getInt("test.usersPerSecond")
  val rampUp = config.getInt("test.rampUp")
  val duration = config.getInt("test.duration")
  val rampDown = config.getInt("test.rampDown")


}


class RunningSimulation extends KeycloakSimulation with TokenAccess with ClientAccess with UserAccess {

  val Options = new Options(config)

  def createUserScenario = {
    scenario("create_user")
      .exec(s => {
        val realm = Realms.getARandomRealm()
        s.set("realm", realm)
      })
      .exec(getAdminToken()).pause(2 seconds)
      .exec({
        val username = UUID.randomUUID().toString
        exec(createUser("${realm}", username)).pause(2 seconds).exec(updatePassword("${realm}", username, "test_user"))
      })

  }

  def createClientScenario = {
    scenario("create_client")
      .exec(s => {
        val realm = Realms.getARandomRealm()
        s.set("realm", realm)
      })
      .exec(getAdminToken())
      .exec({
        val client = UUID.randomUUID().toString
        exec(createClient("${realm}", client))
      })
  }


  def accessTokenScenario = {
    scenario("access_token")
      .exec(s => {
        val realm = Realms.getARandomRealm()
        s.set("realm", realm)
      })
      .exec(getTokens("${realm}", "test_client", "test_user", "test_user"))
      .asLongAs(s => s("status").validate[Int].map(_ == 200 && Random.nextInt(100) < Options.refreshTokenProbability)) {
        pause(Options.refreshTokenPeriod seconds, NormalWithPercentageDuration(Options.refreshTokenPeriod / 10d))
          .exec(refreshToken("${realm}", "test_client", "test_user"))
      }
  }


  def run(scenario: ScenarioBuilder, opsPerSecond: Double) = scenario.inject(
    heavisideUsers(math.ceil(opsPerSecond).toInt) over (Options.duration)
  ).protocols(httpProtocol)


  setUp(
    createUserScenario.inject(heavisideUsers(math.ceil(Options.usersPerSecond * 0.05d).toInt) over (Options.duration)).protocols(httpProtocol),
    createClientScenario.inject(heavisideUsers(math.ceil(Options.usersPerSecond * 0.05d).toInt) over (Options.duration)).protocols(httpProtocol),
    accessTokenScenario.inject(heavisideUsers(math.ceil(Options.usersPerSecond * 0.90d).toInt) over (Options.duration)).protocols(httpProtocol)
  ) /*.maxDuration(Options.rampUp + Options.duration + Options.rampDown)*/

}
