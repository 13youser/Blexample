package com.example.blexample.di

import com.example.blexample.ui.viewmodel.DeviceScanViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/** Koin app module */
val appModule = module {

    viewModel { DeviceScanViewModel() }

}