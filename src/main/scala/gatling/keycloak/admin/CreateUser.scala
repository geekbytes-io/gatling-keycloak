package gatling.keycloak.admin

import gatling.keycloak.KeycloakSimulation
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

trait CreateUser {
  _: KeycloakSimulation =>

  def createUser(realm: String, username: String) = {
    http(s"user_${realm}_${username}_create")
      .post(s"/auth/admin/realms/$realm/users")
      .headers(adminHeaders)
      .body(StringBody(s"""{"enabled":true,"attributes":{},"username":"$username","emailVerified":true}"""))
      .check(headerRegex(HttpHeaderNames.Location, s"""$hostUrl/auth/admin/realms/$realm/users/(.*)""").ofType[String].saveAs(s"${realm}_userId"))
  }


  def setUserId(realm: String, username: String): HttpRequestBuilder = {
    http(s"user_${realm}_${username}_set")
      .get(s"/auth/admin/realms/$realm/users?first=0&max=20")
      .headers(adminHeaders)
      .check(jsonPath(s"$$[?(@.username=='$username')].id").get.ofType[String].saveAs(s"${realm}_userId"))
  }

  def updatePassword(realm: String, username: String, password: String): HttpRequestBuilder = {
    http(s"update_password_${realm}_$username")
      .put(s"/auth/admin/realms/$realm/users/$${${realm}_userId}/reset-password")
      .headers(adminHeaders)
      .body(StringBody(s"""{"type":"password","value":"$password","temporary":false}"""))
      .check()
  }
}
