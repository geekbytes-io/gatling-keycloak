package gatling.keycloak.admin

import gatling.keycloak.KeycloakSimulation
import io.gatling.core.Predef.exec
import io.gatling.core.Predef.scenario

trait SetInfrastructure extends CreateClient with CreateUser with CreateRealm {
	_: KeycloakSimulation =>
	
	def setInfrastructure(
		                     start: Int,
		                     end: Int,
		                     getRealmName: (Int) => String = i => s"try_test_realm_${i}",
		                     getClientID: (Int) => String = _ => "test_client",
		                     getUsername: (Int) => String = _ => "test_user",
		                     getPassword: (Int) => String = _ => "test_user"
	                     ) = {
		val startInner = start * 10 + 1
		val endInner = end * 10 + 10
		val innerRange = Range.inclusive(startInner, endInner)
		execBatch(start.formatted("%04d%n"), innerRange.toList, getRealmName, getClientID, getUsername, getPassword)
	}
	
	def execBatch(
		             start: String,
		             innerRange: List[Int],
		             getRealmName: (Int) => String = i => s"try_test_realm_${i}",
		             getClientID: (Int) => String = _ => "test_client",
		             getUsername: (Int) => String = _ => "test_user",
		             getPassword: (Int) => String = _ => "test_user"
	             ) = {
		scenario(s"set_infrastructure_$start")
			.exec(getAdminToken())
			.exec(
				innerRange.map(iteration => {
					val realm = getRealmName(iteration)
					val clientId = getClientID(iteration)
					val username = getUsername(iteration)
					val password = getPassword(iteration)
					exec(createRealm(realm))
						.exec(
							exec(createClient(realm, clientId))
								.exitBlockOnFail(exec(setClientId(realm, clientId)))
						)
						.exec(
							exec(createUser(realm, username))
								.exitBlockOnFail(exec(setUserId(realm, username)))
						)
						.exec(updatePassword(realm, username, password))
				})
			)
	}
}
