package com.cyberark.identity.data.model

data class EnrollmentModel(
        val success: Boolean,
        val CustomerID: String,
        val CustomerName: String,
        val UserName: String
)
