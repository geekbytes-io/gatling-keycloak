package gatling.keycloak.admin

import gatling.keycloak.KeycloakSimulation
import io.gatling.core.Predef.exec
import io.gatling.core.Predef.scenario
import io.gatling.core.structure.ChainBuilder

trait SetInfrastructure extends ClientAccess with CreateUser with CreateRealm {
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
					setRealm(getRealmName(iteration), getClientID(iteration), getUsername(iteration), getPassword(iteration))
				})
			)
	}
	
	def setRealm(realm: String, clientId: String, username: String, password: String): ChainBuilder = {
		exec(createRealm(realm))
			.exec(setClient(realm, clientId))
			.exec(setUser(realm, username))
			.exec(updatePassword(realm, username, password))
	}
	
	def setUser(realm: String, username: String): ChainBuilder = {
		exec(createUser(realm, username))
			.exitBlockOnFail(exec(getUserId(realm, username)))
	}
	
	def setClient(realm: String, clientId: String): ChainBuilder = {
		exec(createClient(realm, clientId))
			.exitBlockOnFail(exec(getClientId(realm, clientId)))
	}
	
	
	
}
