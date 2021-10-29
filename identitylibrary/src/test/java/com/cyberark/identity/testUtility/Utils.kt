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

package com.cyberark.identity.testUtility

import java.math.BigInteger

object Utils {
    /**
     * This method converts a HEX string to Byte[]
     *
     * @param hex: the HEX string
     * @return a byte array
     */
    @JvmStatic
    fun hexStr2Bytes(hex: String): ByteArray {
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        val bArray = BigInteger("10$hex", 16).toByteArray()

        // Copy all the REAL bytes, not the "first"
        val ret = ByteArray(bArray.size - 1)
        for (i in ret.indices) ret[i] = bArray[i + 1]
        return ret
    }
}