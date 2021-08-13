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

package com.cyberark.identity.util

/**
 * Response handler
 *
 * @param T
 * @property status
 * @property data
 * @property message
 * @constructor Create empty Response handler
 */
data class ResponseHandler<out T>(val status: ResponseStatus, val data: T?, val message: String?) {

    companion object {
        fun <T> success(data: T?): ResponseHandler<T> {
            return ResponseHandler(ResponseStatus.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): ResponseHandler<T> {
            return ResponseHandler(ResponseStatus.ERROR, data, msg)
        }

        fun <T> loading(data: T?): ResponseHandler<T> {
            return ResponseHandler(ResponseStatus.LOADING, data, null)
        }
    }
}