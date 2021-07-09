package com.cyberark.identity.data.model

data class RefreshTokenModel(
        val access_token: String,
        val token_type: String,
        val expires_in: Double,
        val scope: String
)
