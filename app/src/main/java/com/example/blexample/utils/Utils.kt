package com.example.blexample.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

object Utils {

    private var okDialog: AlertDialog? = null

    fun ensureOkDialogDismissed() {
        okDialog?.dismiss()
    }

    fun showOkDialog(context: Context, title: String, message: String) {
        ensureOkDialogDismissed()

        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setNegativeButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
        }

        okDialog = builder.create()
        okDialog?.show()
    }
}