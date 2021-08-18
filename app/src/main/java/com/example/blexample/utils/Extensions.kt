package com.incotex.mercurycashbox.ui.base


import android.view.View


fun View.invisible(b: Boolean) { visibility = if (b) View.INVISIBLE else View.VISIBLE }
fun View.gone(b: Boolean) { visibility = if (b) View.GONE else View.VISIBLE }
