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
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TOTP8DigitsSha256Test(private var input: Int, private var expected: String) {
    private val period = 30
    private val digits = 8

    // HMAC-SHA1 - 32 bytes
    private val secret = "3546423345353930453737424536463233303643424136433445384538453445"

    @Test
    fun test() {
        val totp = TOTPGenerator("sha1", hexStr2Bytes(secret), digits, period)
        Assert.assertEquals(expected, totp.generate(input.toLong()))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(0, "91734617"),
                arrayOf(1, "72096973"),
                arrayOf(2, "28921090"),
                arrayOf(3, "11506418"),
                arrayOf(4, "54352501")
            )
        }
    }
}