package com.example.blexample.ui.base

import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

abstract class BaseFragment : Fragment() {

    protected val TAG = this::class.simpleName

    /**
     * Navigate pop back stack
     * */
    protected fun View.navigateBack() {
        this.findNavController()//.getBackStackEntry()
            .popBackStack()
            .also { println("nav:> popBackStack from $TAG") }
    }

    /**
     * Navigate pop back stack
     * */
    protected fun Fragment.navigateBack() {
        this.findNavController()//.getBackStackEntry()
            .popBackStack()
            .also { println("nav:> popBackStack from $TAG") }
    }

    /**
     * Navigate by ID
     * */
    protected fun View.navigateAction(id: Int) {
        this.findNavController()
            .navigate(
                id
                    .also { println("nav:> from $TAG to id=$id") }
            )
    }

    /**
     * Navigate by ID
     * */
    protected fun Fragment.navigateAction(id: Int) {
        this.findNavController()
            .navigate(
                id
                    .also { println("nav:> from $TAG to id=$id") }
            )
    }

    /**
     * Navigate by Deep Link
     * */
    protected fun View.navigateAction(uri: Uri) {
        this.findNavController()
            .navigate(
                uri
                    .also { println("nav:> from $TAG deepLink=$uri") }
            )
    }

    /**
     * Navigate by Deep Link
     * */
    protected fun Fragment.navigateAction(uri: Uri) {
        this.findNavController()
            .navigate(
                uri
                    .also { println("nav:> from $TAG deepLink=$uri") }
            )
    }
}