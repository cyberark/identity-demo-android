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

import com.cyberark.identity.testUtility.Utils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TOTP6DigitsSha1Test(private var input: Int, private var expected: String) {
    private val period = 30
    private val digits = 6

    // HMAC-SHA1 - 20 bytes
    private val secret = "3546423345353930453737424536463233303643"

    @Test
    fun test() {
        val totp = TOTPGenerator("sha1", Utils.hexStr2Bytes(secret), digits, period)
        Assert.assertEquals(expected, totp.generate(input.toLong()))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(0, "767183"),
                arrayOf(1, "442601"),
                arrayOf(2, "308763"),
                arrayOf(3, "195283"),
                arrayOf(4, "726791"),
                arrayOf(5, "829867"),
                arrayOf(6, "141684"),
                arrayOf(7, "401788"),
                arrayOf(8, "528319"),
                arrayOf(9, "181205")
            )
        }
    }
}