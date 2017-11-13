package gatling.keycloak

import io.gatling.http.Predef._
import io.gatling.core.Predef._

trait TokenAccess {
  _: KeycloakSimulation =>
  def getTokens(realm: String, clientId: String, username: String, password: String) = {
    http(s"token_${realm}_${clientId}_${username}")
      .post(s"/auth/realms/$realm/protocol/openid-connect/token")
      .formParam("client_id", clientId)
      .formParam("username", username)
      .formParam("password", password)
      .formParam("grant_type", "password")
      .check(jsonPath("$.access_token").saveAs(s"${realm}_${clientId}_${username}_token"), jsonPath("$.refresh_token").saveAs(s"${realm}_${clientId}_${username}_refresh_token"))
  }

  def refreshToken(realm: String, clientId: String, username: String) = {
    http(s"token_${realm}_${clientId}_${username}_refresh")
      .post(s"/auth/realms/$realm/protocol/openid-connect/token")
      .formParam("client_id", clientId)
      .formParam("refresh_token", s"$${${realm}_${clientId}_${username}_refresh_token}")
      .formParam("grant_type", "refresh_token")
      .check(jsonPath("$.access_token").saveAs(s"${realm}_${clientId}_${username}_token"), jsonPath("$.refresh_token").saveAs(s"${realm}_${clientId}_${username}_refresh_token"))
  }
}
