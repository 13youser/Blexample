package com.example.blexample.di

import com.example.blexample.data.local.Preferences
import com.example.blexample.ui.viewmodel.DeviceViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/** Koin app module */
val appModule = module {

    single { Preferences.getInstance(get()) }

    viewModel { DeviceViewModel(get()) }

}