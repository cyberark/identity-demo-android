package com.cyberark.identity.data.model

data class EnrollmentResult(
    val Message: String,
    val Status: String,
    val ClientApiKey: String,
    val ErrorCode: String
)
