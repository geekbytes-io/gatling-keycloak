package gatling.keycloak

import java.io.File

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.core.config.GatlingConfiguration
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder
import org.apache.logging.log4j.scala.Logging

/*
* Copyright (c) 2017 Call Handling Services [http://www.callhandling.co.uk]
* Created by mts.manu
* 11/6/2017
*/


trait KeycloakSimulation extends Simulation with Logging {
	
	val config = ConfigFactory.load().withFallback(ConfigFactory.parseFile(new File("gatling-akka-defaults.conf")).withFallback(ConfigFactory.parseFile(new File("gatling-defaults.conf"))))
	
	val gatlingconfig = GatlingConfiguration.load()
	
	
	
	val httpProtocol: HttpProtocolBuilder = http(gatlingconfig)
		.baseURL(hostUrl)
		.inferHtmlResources()
		.acceptEncodingHeader(config.getString("gatling.http.headers.acceptEncoding"))
		.acceptLanguageHeader(config.getString("gatling.http.headers.acceptLanguage"))
		.userAgentHeader(config.getString("gatling.http.headers.userAgent"))
	
	val adminHeaders = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Authorization" -> ("Bearer ${admin_token}"),
		"Content-Type" -> "application/json;charset=UTF-8",
		"Origin" -> config.getString("keycloak.resourceServer.origin")
	)
	
	protected lazy val hostUrl = config.getString("keycloak.host")
	
	def getAdminToken(): HttpRequestBuilder = {
		http(s"get_admin_token")
			.post("/auth/realms/master/protocol/openid-connect/token")
			.formParam("client_id", "security-admin-console")
			.formParam("username", config.getString("keycloak.admin.username"))
			.formParam("password", config.getString("keycloak.admin.password"))
			.formParam("grant_type", "password")
			.check(jsonPath("$.access_token").saveAs("admin_token"))
	}
	
}
