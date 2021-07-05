package com.cyberark.identity.data.model

data class AuthCodeFlowModel(
        val access_token: String,
        val token_type: String,
        val refresh_token: String,
        val state: String,
        val expires_in: Double,
        val scope: String
)
