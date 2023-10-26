package com.fylora

import com.fylora.data.requests.AuthRequest
import com.fylora.data.response.AuthResponse
import com.fylora.data.user.User
import com.fylora.data.user.UserDataSource
import com.fylora.security.hashing.HashingService
import com.fylora.security.hashing.SaltedHash
import com.fylora.security.token.TokenClaim
import com.fylora.security.token.TokenConfig
import com.fylora.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("signup") {
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        if(request.username.length < 3) {
            call.respond(
                HttpStatusCode.Conflict,
                message = "The username cannot be less than 3 characters"
            )
            return@post
        }
        if(userDataSource.getUserByUsername(request.username) != null) {
            call.respond(
                HttpStatusCode.Conflict,
                message = "The username is already taken"
            )
            return@post
        }
        if(!isStrongPassword(request.password)) {
            call.respond(
                HttpStatusCode.Conflict,
                message = "The password is not strong enough"
            )
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val wasAcknowledged = userDataSource.insertUser(user)
        if(!wasAcknowledged) {
            call.respond(
                HttpStatusCode.Conflict,
                message = "Unknown error occurred"
            )
            return@post
        }

        call.respond(HttpStatusCode.OK)
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username)
        if(user == null) {
            call.respond(
                HttpStatusCode.Conflict,
                "Incorrect username or password"
            )
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if(!isValidPassword) {
            call.respond(
                HttpStatusCode.Conflict,
                "Incorrect username or password"
            )
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )
        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}

fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getUserInfo() {
    authenticate {
        get("info") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your id is $userId")
        }
    }
}

fun isStrongPassword(password: String): Boolean {
    val minLength = 8
    val hasUpperCase = password.any {
        it.isUpperCase()
    }
    val hasLowerCase = password.any {
        it.isLowerCase()
    }
    val hasDigit = password.any {
        it.isDigit()
    }
    val hasSpecialChar = password.any {
        it.isLetterOrDigit().not()
    }

    return password.length >= minLength
            && hasUpperCase
            && hasLowerCase
            && hasDigit
            && hasSpecialChar
}