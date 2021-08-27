package com.example.blexample.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.blexample.R
import com.example.blexample.data.model.LeDeviceData
import com.example.blexample.databinding.ActivityMainBinding
import com.example.blexample.service.BluetoothLeService
import com.example.blexample.ui.viewmodel.DeviceViewModel
import com.example.blexample.utils.SampleGattAttributes
import com.example.blexample.utils.Utils
import com.incotex.mercurycashbox.ui.base.gone
import com.incotex.mercurycashbox.ui.base.invisible
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModel<DeviceViewModel>()
    private var bluetoothService : BluetoothLeService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null)
            initBroadcastReceivers()

        initToolbar()
        initServices()
        observeEvents()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(bluetoothReceiver)
            unregisterReceiver(gattUpdateReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    private fun initBroadcastReceivers() {
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

        val filter2 = IntentFilter()
        filter2.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
        filter2.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        filter2.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        filter2.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
        registerReceiver(gattUpdateReceiver, filter2)
    }

    private fun initToolbar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun initServices() {
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.initialize()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            bluetoothService = null
        }
    }

    private fun observeEvents() {
        viewModel.callbacks = object : DeviceViewModel.Callbacks {
            override fun connect(device: BluetoothDevice) {
                Log.i(TAG, "GATT::> connect")
                showProgress()
                bluetoothService?.connect(device.address)
            }
            override fun disconnect() {
                Log.i(TAG, "GATT::> disconnect")
                bluetoothService?.close()
            }
            override fun handleFoundCharacteristic(characteristic: BluetoothGattCharacteristic) {
                when (characteristic.uuid.toString()) {
                    SampleGattAttributes.ST_UUID_CHARACTERISTIC_1, //TODO
                    SampleGattAttributes.ST_UUID_CHARACTERISTIC_2,
                    SampleGattAttributes.UUID_CHARACTERISTIC_SERIAL_NUMBER_STRING,
                    -> {
                        bluetoothService?.readCharacteristic(
                            characteristic = characteristic,
                            repeat = false
                        )
                    }
                }
            }
        }
    }

    private fun showProgress() {
        binding.progressBarrr.invisible(false)
        binding.overlay.gone(false)
    }

    private fun hideProgress() {
        binding.progressBarrr.invisible(true)
        binding.overlay.gone(true)
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    Log.i(TAG, "BLT:: ACTION_GATT_CONNECTED")

                    intent.getParcelableExtra<BluetoothDevice>(
                        BluetoothLeService.EXTRA_DEVICE_CONNECTED
                    )?.let {
                        println(":> on connect UUID=${it.uuids}")
                        viewModel.currentLeDeviceData = LeDeviceData(it.name, it.address)
                    }

                    hideProgress()
                    Utils.showOkDialog(this@MainActivity, "", "GATT connected")
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    Log.i(TAG, "BLT:: ACTION_GATT_DISCONNECTED")
                    hideProgress()
                    viewModel.currentLeDeviceData = null
                    Utils.showOkDialog(this@MainActivity, "", "GATT disconnected")
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    Log.i(TAG, "BLT:: ACTION_GATT_SERVICES_DISCOVERED")
                    viewModel.handleGattServices(bluetoothService?.getSupportedGattServices(), resources)
                }
                BluetoothLeService.ACTION_DATA_AVAILABLE -> {
                    Log.i(TAG, "BLT:: ACTION_DATA_AVAILABLE")

                    intent.getByteArrayExtra(
                        BluetoothLeService.EXTRA_CHARACTERISTIC
                    )?.let { data: ByteArray ->

                        val hexString: String =
                            data.joinToString(
                                separator = " ",
                                transform = { byte -> String.format("%02X", byte) }
                            )
                        println(":> DATA AVAILABLE :: $hexString")
                        /* TODO use read data */

                        /*// if read data it is a SERIAL NUMBER, we can print it like this
                        val output = StringBuilder("")
                        data.forEach { byte -> output.append(byte.toChar()) }
                        println(":> SERIAL NUMBER, for example: $output")*/



                    }
                }
                else -> {
                    Log.e(TAG, "BLT:: ACTION GATT ERROR")
                    hideProgress()
                    Utils.showOkDialog(this@MainActivity, "", "Some went wrong. Try again")
                }
            }
        }
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