package com.cyberark.mfa.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.mfa.ui.qr.ScanQRCodeViewModel

class ViewModelFactory(private val cyberarkAuthHelper: CyberarkAuthHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanQRCodeViewModel::class.java)) {
            return ScanQRCodeViewModel(cyberarkAuthHelper) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}

