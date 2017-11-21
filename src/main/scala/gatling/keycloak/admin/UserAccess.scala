package gatling.keycloak.admin

import gatling.keycloak.KeycloakSimulation
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

trait UserAccess {
  _: KeycloakSimulation =>

  def createUser(realm: Expression[String], username: Expression[String]) = {
    http(s => s"user_create")
      .post(s => realm(s).map(r =>  s"/auth/admin/realms/$r/users"))
      .headers(adminHeaders)
      .body(StringBody(s =>username(s).map(us => s"""{"enabled":true,"attributes":{},"username":"$us","emailVerified":true}""")))
      .check(headerRegex(HttpHeaderNames.Location, s => realm(s).map(r => s"""$hostUrl/auth/admin/realms/$r/users/(.*)""")).ofType[String].saveAs(s"userId"))
  }


  def getUserId(realm: String, username: String): HttpRequestBuilder = {
    http(s"user_${realm}_${username}_set")
      .get(s"/auth/admin/realms/$realm/users?first=0&max=20")
      .headers(adminHeaders)
      .check(jsonPath(s"$$[?(@.username=='$username')].id").get.ofType[String].saveAs(s"userId"))
  }

  def updatePassword(realm: String, username: Expression[String], password: String): HttpRequestBuilder = {
    http(s => s"update_password")
      .put(s"/auth/admin/realms/$realm/users/$${userId}/reset-password")
      .headers(adminHeaders)
      .body(StringBody(s"""{"type":"password","value":"$password","temporary":false}"""))
      .check()
  }

  def setUser(realm: String, username: String): ChainBuilder = {
    exec(createUser(realm, username))
      .exitBlockOnFail(exec(getUserId(realm, username)))
  }
}
