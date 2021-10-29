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

package com.cyberark.identity.util.notification

import com.cyberark.identity.testUtility.Utils.hexStr2Bytes
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.IllegalArgumentException

class TOTPExceptionTest {

    private val period = 30
    // Seed for HMAC-SHA1 - 20 bytes
    private val secret = "3132333435363738393031323334353637383930"

    @Test
    fun shouldFailWithMoreThanEightDigits() {
        val exception: Exception = assertThrows(IllegalArgumentException::class.java) {
            TOTPGenerator("sha1", hexStr2Bytes(secret), 9, period)
        }
        val expectedMessage = "Unsupported digits. It should not exceed 8"
        val actualMessage = exception.message
        assertTrue(actualMessage!!.contains(expectedMessage))
    }

    @Test
    fun shouldFailWithUnknownAlgorithm() {
        val exception: Exception = assertThrows(IllegalArgumentException::class.java) {
            TOTPGenerator("sha111", hexStr2Bytes(secret), 8, period)
        }
        val expectedMessage = "Unsupported algorithm"
        val actualMessage = exception.message
        assertTrue(actualMessage!!.contains(expectedMessage))
    }

    @Test
    fun shouldFailWithInvalidSecret() {
        val exception: Exception = assertThrows(IllegalArgumentException::class.java) {
            TOTPGenerator("sha256", null, 8, period)
        }
        val expectedMessage = "Missing argument"
        val actualMessage = exception.message
        assertTrue(actualMessage!!.contains(expectedMessage))
    }
}