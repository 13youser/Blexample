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

    val ASCII = mapOf(
        "44" to ".", "46" to ".", "45" to "-",
        "48" to "0", "49" to "1", "50" to "2", "51" to "3", "52" to "4",
        "53" to "5", "54" to "6", "55" to "7", "56" to "8", "57" to "9"
    )

    fun bytesToAsciiString(bytes: ByteArray): String {
        val output = StringBuilder("")
        bytes.forEach { byte -> output.append(byte.toChar()) }
        return output.toString()
    }

    fun bytesToHexString(bytes: ByteArray) = bytes.joinToString(
        separator = " ",
        transform = { byte -> String.format("%02X", byte) }
    )
}