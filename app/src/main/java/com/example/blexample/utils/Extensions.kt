package com.incotex.mercurycashbox.ui.base

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.blexample.R


fun View.invisible(b: Boolean) {
    visibility = if (b) View.INVISIBLE else View.VISIBLE
    println("[View.invisible]:> ${if (b) "View.INVISIBLE" else "View.VISIBLE"}")
}

fun View.gone(b: Boolean) {
    visibility = if (b) View.GONE else View.VISIBLE
    println("[View.isGone]:> ${if (b) "View.GONE" else "View.VISIBLE"}")
}
