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

package com.cyberark.identity.viewmodel.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.viewmodel.AuthenticationViewModel
import com.cyberark.identity.viewmodel.EnrollmentViewModel
import com.cyberark.identity.viewmodel.ScanQRCodeViewModel

class CyberarkViewModelFactory(private val cyberarkAuthHelper: CyberarkAuthHelper) :
        ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthenticationViewModel::class.java)) {
            return AuthenticationViewModel(cyberarkAuthHelper) as T
        } else if (modelClass.isAssignableFrom(ScanQRCodeViewModel::class.java)) {
            return ScanQRCodeViewModel(cyberarkAuthHelper) as T
        } else if (modelClass.isAssignableFrom(EnrollmentViewModel::class.java)) {
            return EnrollmentViewModel(cyberarkAuthHelper) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}

