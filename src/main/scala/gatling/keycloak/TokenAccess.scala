package gatling.keycloak

import io.gatling.commons.validation.Success
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.http.response.HttpResponse

trait TokenAccess {
  _: KeycloakSimulation =>
  def getTokens(realm: Expression[String], clientId: String, username: String, password: String) = {
    http(s => s"token_get")
      .post(s => realm(s).map(r => s"/auth/realms/$r/protocol/openid-connect/token"))
      .formParam("client_id", clientId)
      .formParam("username", username)
      .formParam("password", password)
      .formParam("grant_type", "password")
      .check(status.in(400,200,401).saveAs("status"))
      .check(
        checkIf(s => s("status").validate[Int].map(_ == 401))(
          jsonPath("$.error_description").is("Invalid user credentials")
        ),
        checkIf(s => s("status").validate[Int].map(_ == 400))(
          jsonPath("$.error_description").is("UNKNOWN_CLIENT: Client was not identified by any client authenticator")
        ),
        checkIf(s => s("status").validate[Int].map(_ == 200))(
        jsonPath("$.refresh_token").saveAs("refresh_token"))
      )


  }

  def refreshToken(realm: Expression[String], clientId: String, username: String) = {
    http(s"token_refresh")
      .post(s => realm(s).map(r => s"/auth/realms/$r/protocol/openid-connect/token"))
      .formParam("client_id", clientId)
      .formParam("refresh_token", "${refresh_token}")
      .formParam("grant_type", "refresh_token")
      .check(status.in(200).saveAs("status"))
      .check(jsonPath("$.refresh_token").saveAs("refresh_token"))
  }
}
