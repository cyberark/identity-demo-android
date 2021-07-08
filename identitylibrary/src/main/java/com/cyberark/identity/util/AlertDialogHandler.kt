package com.cyberark.identity.util

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

interface AlertDialogButtonCallback {
    fun tappedButtonwithType(buttonType: AlertButtonType)
}

enum class AlertButtonType {
    POSITIVE,NEGATIVE,NEUTRAL
}

final class AlertButton(title:String,buttonType:AlertButtonType) {
    var buttonTitle = title; private set
    var buttonType = buttonType; private set
}

class AlertDialogHandler(callback: AlertDialogButtonCallback):DialogInterface.OnClickListener {
    private lateinit var alertDialog:AlertDialog
    private val callback = callback
    private var cancellable:Boolean = true

    fun displayAlert(activity:Activity,title:String,message:String,cancellable:Boolean,pButtons:List<AlertButton> ): AlertDialog {
        var alerBuilder = AlertDialog.Builder(activity)
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

    fun dismissAlert() {
        if (this.cancellable) {
            alertDialog?.dismiss()
        }
    }

    fun dismiss() {
        if (this.cancellable) {
            alertDialog?.dismiss()
        }
    }

    fun dismissForcefully() {
        alertDialog?.dismiss()
    }


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

