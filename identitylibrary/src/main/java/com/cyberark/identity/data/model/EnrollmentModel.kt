package com.cyberark.identity.data.model

data class EnrollmentModel(
        val success: Boolean,
        val Result: EnrollmentResult,
        val Message: String,
        val MessageID: String,
        val Exception: String,
        val ErrorID: String,
        val ErrorCode: String,
        val IsSoftError: Boolean,
        val InnerExceptions: String
)
