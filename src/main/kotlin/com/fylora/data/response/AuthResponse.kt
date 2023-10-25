package com.fylora.data.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse (
    val token: String
)