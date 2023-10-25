package com.fylora.plugins

import com.fylora.authenticate
import com.fylora.data.user.UserDataSource
import com.fylora.getUserInfo
import com.fylora.security.hashing.HashingService
import com.fylora.security.token.TokenConfig
import com.fylora.security.token.TokenService
import com.fylora.signIn
import com.fylora.signUp
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        get("/") {
            call.respondText("dayum")
        }
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(hashingService, userDataSource)
        authenticate()
        getUserInfo()
    }
}
