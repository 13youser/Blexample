package com.example.blexample.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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
import java.nio.charset.StandardCharsets

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
        filter2.addAction(BluetoothLeService.ACTION_RESULT_CHARA_READ)
        filter2.addAction(BluetoothLeService.ACTION_RESULT_CHARA_WRITE)
        filter2.addAction(BluetoothLeService.ACTION_RESULT_CHARA_NOTIFICATION)
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

    var count = 0

    private fun observeEvents() {
        viewModel.callbacks = object : DeviceViewModel.Callbacks {
            override fun connect(device: BluetoothDevice) {
                Log.i(TAG, "GATT::> connect")
                showProgress()
                bluetoothService?.connect(device.address)
            }
            override fun disconnect() {
                Log.i(TAG, "GATT::> disconnect")
                bluetoothService?.disconnect()
            }
            override fun handleFoundCharacteristic(characteristic: BluetoothGattCharacteristic) {
                val props: Int = characteristic.properties

                /* TODO
                    как прочитать из характеристики свойства в случае двух свойств, например?
                */

                //todo
                /*when(props) {
                    BluetoothGattCharacteristic.PROPERTY_BROADCAST -> {
                    }
                    BluetoothGattCharacteristic.PROPERTY_READ -> {
                        count++

                        Handler().postDelayed(
                            {
                                bluetoothService?.readCharacteristic(
                                    characteristic = characteristic,
                                    repeat = false
                                )
                            },
                            1000L * count // because reading often - fails
                        )
                    }
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE -> {
                    }
                    BluetoothGattCharacteristic.PROPERTY_WRITE -> {
                    }
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY -> {
                    }
                    BluetoothGattCharacteristic.PROPERTY_INDICATE -> {
                    }
                    BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE -> {
                    }
                    BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS -> {
                    }
                    else -> println(":> Unknown PROPERTY")
                }*/


                /*when (chara.uuid.toString()) { //TODO use
                    SampleGattAttributes.ST_UUID_CHARACTERISTIC_2,
                    -> {
                        count++
                        chara.value = "Hello BLE ${count}\n".toByteArray()

                        bluetoothService?.writeCharacteristic(
                            characteristic = chara,
                            repeat = false
                        )
                    }
                }*/



                if (characteristic.uuid.toString() ==
                    SampleGattAttributes.UUID_CHARACTERISTIC_DEVICE_NAME) {

                        if (characteristic.properties ==
                            BluetoothGattCharacteristic.PROPERTY_READ) {

                            bluetoothService?.readCharacteristic(
                                characteristic = characteristic,
                                repeat = false
                            )
                        }
                }


                //TODO: read weight
                when (characteristic.uuid.toString()) {
                    SampleGattAttributes.UUID_CHARACTERISTIC_INCOTEX_WS_SCALES_02,
                    -> {
                        count++

//                        println(":> Read bytes started")
                        println(":> Notif bytes started")

                        Handler().postDelayed(
                            {
//                                bluetoothService?.readCharacteristic(
//                                    characteristic = characteristic,
//                                    repeat = true
//                                )
//                                bluetoothService?.subscribeNotifications(
//                                    characteristic = characteristic,
//                                )

                                bluetoothService?.setCharacteristicNotification(characteristic, true)
                            },
                            1000L * count // because reading often - fails
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
                    Utils.showOkDialog(this@MainActivity, "",
                        "GATT connected")
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    Log.i(TAG, "BLT:: ACTION_GATT_DISCONNECTED")
                    hideProgress()
                    bluetoothService?.disconnect()
                    viewModel.currentLeDeviceData = null
                    Utils.showOkDialog(this@MainActivity, "",
                        "GATT disconnected")
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    Log.i(TAG, "BLT:: ACTION_GATT_SERVICES_DISCOVERED")
                    viewModel.handleGattServices(
                        bluetoothService?.getSupportedGattServices(),
                        resources
                    )
                }
                BluetoothLeService.ACTION_RESULT_CHARA_READ -> {
                    Log.i(TAG, "BLT:: ACTION_RESULT_CHARA_READ")

                    intent.getByteArrayExtra(
                        BluetoothLeService.EXTRA_CHARACTERISTIC
                    )?.let { data: ByteArray ->

//                        printTestAscii(data, "R-read")

                        println(":> R-read data hex: ${Utils.bytesToHexString(bytes = data)}")
                        println(":> R-read data ascii: ${Utils.bytesToAsciiString(bytes = data)}")
                    }
                }
                BluetoothLeService.ACTION_RESULT_CHARA_WRITE -> {
                    Log.i(TAG, "BLT:: ACTION_RESULT_CHARA_WRITE")

                    intent.getByteArrayExtra(
                        BluetoothLeService.EXTRA_CHARACTERISTIC
                    )?.let { data: ByteArray ->
                        println(":> W-Sent data hex: ${Utils.bytesToHexString(bytes = data)}")
                        println(":> W-Sent data ascii: ${Utils.bytesToAsciiString(bytes = data)}")
                    }
                }
                BluetoothLeService.ACTION_RESULT_CHARA_NOTIFICATION -> {
                    Log.i(TAG, "BLT:: ACTION_RESULT_CHARA_NOTIFICATION")

                    intent.getByteArrayExtra(
                        BluetoothLeService.EXTRA_CHARACTERISTIC
                    )?.let { data: ByteArray ->
//                        println(":> Notif data bytes.toString(): $data}")


//                        printTestAscii(data, "Notif")

                        println(":> Notif data hex: ${Utils.bytesToHexString(bytes = data)}")
                        println(":> Notif data ascii: ${Utils.bytesToAsciiString(bytes = data)}")

//                        val stri: String = String(data, StandardCharsets.UTF_8)
//                        val stri: String = String(data, StandardCharsets.UTF_16)
//                        val stri: String = String(data, StandardCharsets.UTF_16BE)
//                        val stri: String = String(data, StandardCharsets.UTF_16LE)
//                        val stri: String = String(data, StandardCharsets.US_ASCII)
//                        val stri: String = String(data, StandardCharsets.ISO_8859_1)
//                        println(":> Notif data string: $stri")
                    }
                }
                else -> {
                    Log.e(TAG, "BLT:: ACTION GATT ERROR")
                    hideProgress()
                    Utils.showOkDialog(this@MainActivity, "",
                        "Some went wrong. Try again")
                }
            }
        }
    }

    private fun printTestAscii(data: ByteArray, tag: String) {
        val humanBuffer: Array<String> = Array(data.size) { "" }
        data.forEachIndexed { index, byte ->
            val ascii: String = getPrintAscii(byte.toString())
            humanBuffer[index] = ascii
        }
        println(":> $tag DDD_AAA_TTT_AAA : ${humanBuffer.contentToString()}")
    }

    val printAscii = mapOf(
        "44" to ".", "46" to ".", "45" to "-",
        "48" to "0", "49" to "1", "50" to "2", "51" to "3", "52" to "4",
        "53" to "5", "54" to "6", "55" to "7", "56" to "8", "57" to "9"
    )
    fun getPrintAscii(inChar: String):String {
//        return printAscii[inChar]?.let { return (it) } ?: return ""
        return printAscii[inChar] ?: ""
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