package com.example.blexample.ui.base

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.blexample.utils.DisposableManager

abstract class BaseViewModel : ViewModel() {

    protected val TAG = this::class.simpleName

    val disposableManager = DisposableManager()

    override fun onCleared() {
        Log.e("observeUsbRequest", "oncleared in VIEW MODELs")
        disposableManager.clear()
    }

}