package com.cyberark.identity.viewmodel.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.viewmodel.AuthenticationViewModel

class CyberarkViewModelFactory(private val cyberarkAuthHelper: CyberarkAuthHelper) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthenticationViewModel::class.java)) {
            return AuthenticationViewModel(cyberarkAuthHelper) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}

