package com.cyberark.mfa.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cyberark.identity.data.network.CyberarkAuthHelper

class ViewModelFactory(private val cyberarkAuthHelper: CyberarkAuthHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        throw IllegalArgumentException("Unknown class name")
    }

}

