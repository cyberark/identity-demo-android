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

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

/**
 * Alert dialog button callback
 *
 * @constructor Create empty Alert dialog button callback
 */
interface AlertDialogButtonCallback {
    /**
     * Tapped buttonwith type
     *
     * @param buttonType
     */
    fun tappedButtonwithType(buttonType: AlertButtonType)
}

/**
 * Alert button type
 *
 * @constructor Create empty Alert button type
 */
enum class AlertButtonType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

/**
 * Alert button
 *
 * @constructor
 *
 * @param title
 * @param buttonType
 */
class AlertButton(title:String, buttonType:AlertButtonType) {
    var buttonTitle = title; private set
    var buttonType = buttonType; private set
}

/**
 * Alert dialog handler
 *
 * @property callback
 * @constructor Create empty Alert dialog handler
 */
class AlertDialogHandler(private val callback: AlertDialogButtonCallback):DialogInterface.OnClickListener {
    private lateinit var alertDialog:AlertDialog
    private var cancellable:Boolean = true

    /**
     * Display alert
     *
     * @param activity
     * @param title
     * @param message
     * @param cancellable
     * @param pButtons
     * @return
     */
    fun displayAlert(activity:Activity, title:String, message:String, cancellable:Boolean, pButtons:List<AlertButton> ): AlertDialog {
        val alerBuilder = AlertDialog.Builder(activity)
        alerBuilder.setTitle(title)
        alerBuilder.setMessage(message)
        this.cancellable = cancellable
        alerBuilder.setCancelable(this.cancellable)
        for (pButton in pButtons) {
            when(pButton.buttonType) {
                AlertButtonType.POSITIVE -> alerBuilder.setPositiveButton(pButton.buttonTitle,this)
                AlertButtonType.NEGATIVE -> alerBuilder.setNegativeButton(pButton.buttonTitle,this)
                AlertButtonType.NEUTRAL -> alerBuilder.setNeutralButton(pButton.buttonTitle,this)
            }
        }
//        AlertButton("OK",true)
//        for (nButton in nButtons) {
//            if ()
//            alerBuilder.setPositiveButton(nButton, this)
//        }
        alertDialog = alerBuilder.create()
        alertDialog.show()
        return alertDialog
    }

    /**
     * Dismiss alert
     *
     */
    fun dismissAlert() {
        if (this.cancellable) {
            alertDialog.dismiss()
        }
    }

    /**
     * Dismiss
     *
     */
    fun dismiss() {
        if (this.cancellable) {
            alertDialog.dismiss()
        }
    }

    /**
     * Dismiss forcefully
     *
     */
    fun dismissForcefully() {
        alertDialog.dismiss()
    }

    /**
     * On click
     *
     * @param dialog
     * @param which
     */
    override fun onClick(dialog: DialogInterface?, which: Int) {
        print("Dialog button selected")
        var buttonType:AlertButtonType = AlertButtonType.NEUTRAL
        when(which) {
            AlertDialog.BUTTON_POSITIVE->
                buttonType = AlertButtonType.POSITIVE
            AlertDialog.BUTTON_NEGATIVE->
                buttonType = AlertButtonType.NEGATIVE
        }
        callback.tappedButtonwithType(buttonType)
    }

}

