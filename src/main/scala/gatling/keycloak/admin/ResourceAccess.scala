package gatling.keycloak.admin

import gatling.keycloak.KeycloakSimulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._

/*
* Copyright (c) 2017 Call Handling Services [http://www.callhandling.co.uk]
* Created by mts.manu
* 11/8/2017
*/


trait ResourceAccess {
	_: KeycloakSimulation =>
	
	def createResource(realm: String, clientId: String)(name: String, `type`: String, uri: String, scopes: List[String]) = {
		http(s"resource_${realm}_${clientId}_create")
			.post(s"/auth/admin/realms/$realm/clients/$${${realm}_clientId}/authz/resource-server/resource")
			.headers(adminHeaders)
			.body(StringBody(s"""{"scopes":[${scopes.map(s => s""""${s.trim}"""").mkString(",")}],"name":"${name}","type":"${`type`}","uri":"${uri}"}"""))
			.check()
	}
	
	
	
}



