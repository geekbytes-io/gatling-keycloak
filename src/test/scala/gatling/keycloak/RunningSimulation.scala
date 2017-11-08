package gatling.keycloak

import java.util.UUID

import io.gatling.core.Predef._
import scala.concurrent.duration._

/*
* Copyright (c) 2017 Call Handling Services [http://www.callhandling.co.uk]
* Created by mts.manu
* 11/7/2017
*/


class AccessToken_Realms_1To100_10_Users extends KeycloakSimulation with TokenAccess {


  //setUp(Range(0, 50).map(i => (setInfrastructure(i, i).inject(atOnceUsers(1))).protocols(httpProtocol)): _*)
  //setUp(execBatch("failed_201To400", List(342,292,262,352,312,202,272,232,214,345,253,323,354,364,274,316)).inject(atOnceUsers(1)).protocols(httpProtocol))

  setUp(Range.inclusive(1, 10).toList.map(realm => scenario(s"use_token_$realm")
    .exec(getTokens(s"try_test_realm_${realm.toString}", "test_client", "test_user", "test_user"))
    .exec(refreshToken(s"try_test_realm_${realm.toString}", "test_client", "test_user"))
    .inject(rampUsers(10).over(2 seconds)).protocols(httpProtocol)
  ))
}
