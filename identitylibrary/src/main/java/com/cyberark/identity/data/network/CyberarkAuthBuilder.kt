package com.cyberark.identity.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CyberarkAuthBuilder {

    private const val BASE_URL = "https://aaj7617.my.dev.idaptive.app/"

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    val cyberarkAuthService: CyberarkAuthService = getRetrofit().create(CyberarkAuthService::class.java)

}