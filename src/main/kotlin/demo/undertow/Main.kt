package demo.undertow

import io.undertow.Undertow
import io.undertow.security.api.AuthenticationMechanism
import io.undertow.security.api.AuthenticationMode
import io.undertow.security.handlers.AuthenticationCallHandler
import io.undertow.security.handlers.AuthenticationConstraintHandler
import io.undertow.security.handlers.AuthenticationMechanismsHandler
import io.undertow.security.handlers.SecurityInitialHandler
import io.undertow.security.idm.IdentityManager
import io.undertow.security.impl.BasicAuthenticationMechanism
import io.undertow.server.HttpHandler
import io.undertow.server.RoutingHandler
import io.undertow.server.handlers.BlockingHandler
import io.undertow.server.handlers.resource.ClassPathResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.util.Headers

fun main() {
    val users = mapOf(
        "user1" to "password1".toCharArray(),
        "user2" to "password2".toCharArray()
    )

    val identityManager = MapIdentityManager(users)

    val routingHandler = RoutingHandler()
        .get("/helloWorld") {
            it.responseHeaders.put(Headers.CONTENT_TYPE, "text/plain")
            it.responseSender.send("Hello World")
        }
        .get("/securedHelloWorld", addSecurity(BlockingHandler {
            it.responseHeaders.put(Headers.CONTENT_TYPE, "text/plain")
            it.responseSender.send("Hello World bien sécurisé comme il faut.")
        }, identityManager))
        .setFallbackHandler {
            it.statusCode = 404
            it.responseHeaders.put(Headers.CONTENT_TYPE, "text/plain")
            it.responseSender.send("Epic fail")
        }

    val staticFileServing = addSecurity(
        ResourceHandler(ClassPathResourceManager(Thread.currentThread().contextClassLoader)),
        identityManager
    )

    val server = Undertow.builder()
        .addHttpListener(8080, "localhost")
        .setHandler(routingHandler)
        //.setHandler(staticFileServing)
        .build()
    server.start()
}

private fun addSecurity(toWrap: HttpHandler, identityManager: IdentityManager): HttpHandler {
    val authenticationCallHandler = AuthenticationCallHandler(toWrap)
    val authenticationConstraintHandler = AuthenticationConstraintHandler(authenticationCallHandler)
    val mechanisms = listOf<AuthenticationMechanism>(BasicAuthenticationMechanism("My Realm"))
    val authenticationMechanismsHandler = AuthenticationMechanismsHandler(authenticationConstraintHandler, mechanisms)
    return SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, authenticationMechanismsHandler)
}