package gatling.keycloak

import java.util.UUID

import gatling.keycloak.admin.ClientAccess
import gatling.keycloak.admin.ResourceAccess
import io.gatling.core.Predef._

import scala.concurrent.duration._

/*
* Copyright (c) 2017 Call Handling Services [http://www.callhandling.co.uk]
* Created by mts.manu
* 11/7/2017
*/


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
}
