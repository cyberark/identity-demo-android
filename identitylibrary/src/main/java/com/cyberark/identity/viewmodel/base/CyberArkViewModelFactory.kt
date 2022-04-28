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
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.viewmodel.*
import com.cyberark.identity.viewmodel.AuthenticationViewModel
import com.cyberark.identity.viewmodel.AuthenticationWidgetViewModel
import com.cyberark.identity.viewmodel.EnrollmentViewModel
import com.cyberark.identity.viewmodel.ScanQRCodeViewModel
import com.cyberark.identity.viewmodel.SignupWithCaptchaViewModel

/**
 * CyberArk view model factory
 *
 * @property cyberArkAuthHelper: CyberArkAuthHelper instance
 */
@Suppress("UNCHECKED_CAST")
class CyberArkViewModelFactory(private val cyberArkAuthHelper: CyberArkAuthHelper) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthenticationViewModel::class.java) -> {
                AuthenticationViewModel(cyberArkAuthHelper) as T
            }
            modelClass.isAssignableFrom(ScanQRCodeViewModel::class.java) -> {
                ScanQRCodeViewModel(cyberArkAuthHelper) as T
            }
            modelClass.isAssignableFrom(EnrollmentViewModel::class.java) -> {
                EnrollmentViewModel(cyberArkAuthHelper) as T
            }
            modelClass.isAssignableFrom(SignupWithCaptchaViewModel::class.java) -> {
                SignupWithCaptchaViewModel(cyberArkAuthHelper) as T
            }
            modelClass.isAssignableFrom(AuthenticationWidgetViewModel::class.java) -> {
                AuthenticationWidgetViewModel(cyberArkAuthHelper) as T
            }
            else -> throw IllegalArgumentException("Unknown class name")
        }
    }
}
