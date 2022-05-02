/*
 * Copyright (c) 2022 CyberArk Software Ltd. All rights reserved.
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

package com.cyberark.identity.data.model

/**
 * User info model
 *
 * @property auth_time: authentication time
 * @property given_name: given name
 * @property name: name
 * @property email: email Id
 * @property family_name: family name
 * @property preferred_username: preferred username
 * @property unique_name: unique username
 * @property email_verified: email verified status - true/false
 */
data class UserInfoModel(
    val auth_time: String,
    val given_name: String,
    val name: String,
    val email: String,
    val family_name: String,
    val preferred_username: String,
    val unique_name: String,
    val email_verified: String
)
