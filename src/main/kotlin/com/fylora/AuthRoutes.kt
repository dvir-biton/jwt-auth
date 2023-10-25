package com.fylora

import com.fylora.data.requests.AuthRequest
import com.fylora.data.user.User
import com.fylora.data.user.UserDataSource
import com.fylora.security.hashing.HashingService
import io.ktor.http.*
import io.ktor.server.application.*
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
        if(
            request.username.isBlank()
            || !isStrongPassword(request.password)
        ) {
            call.respond(
                HttpStatusCode.Conflict,
                message = "The password is not strong enough"
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

    return password.length > minLength
            && hasUpperCase
            && hasLowerCase
            && hasDigit
            && hasSpecialChar
}