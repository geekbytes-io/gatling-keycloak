package gatling.keycloak.admin

import gatling.keycloak.KeycloakSimulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

/*
* Copyright (c) 2017 Call Handling Services [http://www.callhandling.co.uk]
* Created by mts.manu
* 11/3/2017
*/


trait CreateRealm {
  _: KeycloakSimulation =>


  def createRealm(realm: String): HttpRequestBuilder = {
    http(s"create_realm_${realm}")
      .post("/auth/admin/realms")
      .headers(adminHeaders)
      .body(StringBody(s"""{"enabled":true,"id":"$realm","realm":"$realm"}"""))
      .check()
  }


}
