package gatling.keycloak.admin

import gatling.keycloak.KeycloakSimulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

/*
* Copyright (c) 2017 Call Handling Services [http://www.callhandling.co.uk]
* Created by mts.manu
* 11/6/2017
*/


trait ClientAccess {
  _: KeycloakSimulation =>


  def createClient(realm: String, clientId: String): HttpRequestBuilder = {
    http(s"client_${realm}_${clientId}_create")
      .post(s"/auth/admin/realms/$realm/clients")
      .headers(adminHeaders)
      .body(StringBody(s"""{"enabled":true,"attributes":{},"redirectUris":[],"clientId":"$clientId","rootUrl":"http://10.0.0.27/","protocol":"openid-connect"}"""))
      .check(headerRegex(HttpHeaderNames.Location, s"""$hostUrl/auth/admin/realms/$realm/clients/(.*)""").ofType[String].saveAs(s"${realm}_clientId"))
  }

  def getClientId(realm: String, clientId: String): HttpRequestBuilder = {
    http(s"client_${realm}_${clientId}_set")
      .get(s"/auth/admin/realms/$realm/clients?viewableOnly=true")
      .headers(adminHeaders)
      .check(jsonPath(s"$$[?(@.clientId=='$clientId')].id").get.ofType[String].saveAs(s"${realm}_clientId"))
  }
  
  
  def enableAuthorizationServicesOnClient(realm: String, clientId: String) = {
    http(s"client_${realm}_enableAuth")
      .put(s"/auth/admin/realms/$realm/clients/$${${realm}_clientId}")
      .headers(adminHeaders)
      .body(StringBody(s"""{"clientId": "test_client","serviceAccountsEnabled": true,"authorizationServicesEnabled": true}"""))
      .check()
  }
  
  


}
