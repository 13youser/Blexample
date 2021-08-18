package com.example.blexample.ui.base

import androidx.lifecycle.ViewModel
import com.example.blexample.utils.DisposableManager


abstract class BaseViewModel : ViewModel() {

    protected val TAG = this::class.simpleName

    val disposableManager = DisposableManager()

    override fun onCleared() {
        disposableManager.clear()
    }

}