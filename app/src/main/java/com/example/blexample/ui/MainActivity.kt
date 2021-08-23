package com.example.blexample.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.blexample.R
import com.example.blexample.databinding.ActivityMainBinding
import com.example.blexample.ui.viewmodel.DeviceScanViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModelScan by viewModel<DeviceScanViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            initBluetoothReceiver()
        }
        initToolbar()
    }

    private fun initBluetoothReceiver() {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bluetoothReceiver, filter)
    }

    private fun initToolbar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(bluetoothReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    println("BLT ACTION_DISCOVERY_STARTED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    println("BLT ACTION_DISCOVERY_FINISHED")
                }
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                    println("BLT ACTION_SCAN_MODE_CHANGED")
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    println("BLT ACTION_STATE_CHANGED")
                    val state: Int = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR
                    )
                    /*val statePrevious: Int = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.ERROR
                    )*/
                    when(state) {
                        BluetoothAdapter.ERROR -> {
                            println("BLT ERROR")
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            println("BLT STATE_OFF")
                        }
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            println("BLT STATE_TURNING_OFF")
                        }
                        BluetoothAdapter.STATE_ON -> {
                            println("BLT STATE_ON")
                        }
                        BluetoothAdapter.STATE_TURNING_ON -> {
                            println("BLT STATE_TURNING_ON")
                        }
                    }
                }
                BluetoothDevice.ACTION_FOUND -> {
                    println("BLT_DEVICE ACTION_FOUND")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> {
                    println("BLT_DEVICE ACTION_ACL_DISCONNECT_REQUESTED")
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    println("BLT_DEVICE ACTION_ACL_CONNECTED")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    println("BLT_DEVICE ACTION_ACL_DISCONNECTED")
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    println("BLT_DEVICE ACTION_BOND_STATE_CHANGED")
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { d ->
                        when (d.bondState) {
                            BluetoothDevice.BOND_NONE -> {
                                println("BLT_DEVICE BOND_NONE")
                            }
                            BluetoothDevice.BOND_BONDING -> {
                                println("BLT_DEVICE BOND_BONDING")
                            }
                            BluetoothDevice.BOND_BONDED -> {
                                println("BLT_DEVICE BOND_BONDED")
                            }
                        }
                    }
                }
            }
        }

    }
}