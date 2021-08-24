/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyberark.identity.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * CyberArk auth builder is the base class for API call
 *
 */
object CyberArkAuthBuilder {

    /**
     * Default Base URL
     */
    private const val BASE_URL = "https://aaj7617.my.dev.idaptive.app/"

    /**
     * Get retrofit builder instance for base URL
     *
     * @return retrofit builder instance
     */
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    /**
     * CyberArk auth service instance
     */
    val CYBER_ARK_AUTH_SERVICE: CyberArkAuthService = getRetrofit().create(CyberArkAuthService::class.java)

}