package com.example.blexample.ui.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.res.Resources
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.blexample.R
import com.example.blexample.data.model.LeDeviceData
import com.example.blexample.data.local.Preferences
import com.example.blexample.ui.base.BaseViewModel
import com.example.blexample.utils.SampleGattAttributes
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

/** Scanning and current */
class DeviceViewModel(
    private val prefs: Preferences,
) : BaseViewModel() {

    private companion object {
        const val SCAN_PERIOD: Long = 13000

        const val LIST_NAME = "NAME"
        const val LIST_UUID = "uuid"
    }

    var callbacks: Callbacks? = null

    interface Callbacks {
        fun connect(device: BluetoothDevice)
        fun disconnect()
        fun handleFoundCharacteristic(characteristic: BluetoothGattCharacteristic)
    }

    private val callableStartScanning = Callable<Unit> {
        println(":> call start scan le -- (2)")
        leScanner?.startScan(leScanCallback)
    }
    private val callableStopScanning = Callable<Unit> {
        leScanner?.stopScan(leScanCallback)
    }
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var leScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            _scanResultDevice.value = result.device
            println(":> call start scan le -- found device  ${result.device}")

            val d = result.device
            d.uuids
        }
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            println(":> call start scan le -- Failed code $errorCode")
        }

    }
    var currentLeDeviceData: LeDeviceData? = null
        get() = _currentDeviceLiveData.value
            ?: prefs.leDeviceData
                .also { _currentDeviceLiveData.value = it }
        set(value) {
            field = value
            prefs.leDeviceData = value
            _currentDeviceLiveData.value = value
        }
    private var listGattCharacteristics = mutableListOf<ArrayList<BluetoothGattCharacteristic>>()
    private val handler = Handler()
    private val runnableForPendingStopScanning = Runnable { callStopScanLe() }

    private val _scanningCalled = MutableLiveData<Boolean>()
    val scanningCalled: LiveData<Boolean> get() = _scanningCalled

    private val _scanResultDevice = MutableLiveData<BluetoothDevice>()
    val scanResultDevice: LiveData<BluetoothDevice> get() = _scanResultDevice

    private val _currentDeviceLiveData = MutableLiveData<LeDeviceData?>()
        .also { it.value = prefs.leDeviceData }
    val currentLeDeviceLiveData: LiveData<LeDeviceData?> get() = _currentDeviceLiveData

    fun forgetDevice() {
        callbacks?.disconnect()
        currentLeDeviceData = null
    }

    private fun callStartScanLe() {
        println(":> call start scan le")
        _scanningCalled.value = true

        handler.postDelayed(runnableForPendingStopScanning, SCAN_PERIOD)

        disposableManager.add(
            Completable.fromCallable(callableStartScanning)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { Log.d(TAG, "scan:: SUCCESS-START---Callable") },
                    { Log.e(TAG, "scan:: FAILED-START---Callable") }
                )
        )
    }

    private fun callStopScanLe() {
        _scanningCalled.value = false

        handler.removeCallbacks(runnableForPendingStopScanning)

        disposableManager.add(
            Completable.fromCallable(callableStopScanning)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { Log.d(TAG, "scan:: SUCCESS-STOP---Callable") },
                    { Log.e(TAG, "scan:: FAILED-STOP---Callable") }
                )
        )
    }

    /**
     * Check Bluetooth enabled
     * @return true if enabled, null if Bluetooth is not supported on this hardware platform
     */
    fun isBluetoothEnabled() = bluetoothAdapter?.isEnabled

    /**
     * Start BLE devices scanning if it has not been started yet, otherwise stop
     */
    fun switchScanLeDevice() {
        if (_scanningCalled.value != true) { // Stops scanning after a pre-defined scan period.
            callStartScanLe()
        } else {
            callStopScanLe()
        }
    }

    /**
     * Stop BLE devices scanning
     */
    fun stopScanLeDevice() {
        callStopScanLe()
    }

    /**
     * Connect to BLE device
     */
    fun tryConnect(device: BluetoothDevice) {
        stopScanLeDevice()
        rememberData(device)
        callbacks?.connect(device)
    }

    private fun rememberData(device: BluetoothDevice) {
        currentLeDeviceData = LeDeviceData(device.name, device.address)
    }

    /**
     * Iterate through the supported GATT
     * */
    fun handleGattServices(gattServices: List<BluetoothGattService?>?, resources: Resources) {
        if (gattServices == null) return

        val unknownServiceString: String = resources.getString(R.string.unknown_service)
        val unknownCharaString: String = resources.getString(R.string.unknown_characteristic)

        //val gattServiceData = mutableListOf<HashMap<String, String>>() //TODO S
        //val gattCharacteristicData = mutableListOf<ArrayList<HashMap<String, String>>>() //TODO C

        // Loops through available GATT Services.
        var countServise = 0
        gattServices.forEach { gattService ->
            val currentServiceData = hashMapOf<String, String>()
            gattService?.uuid.toString().let { uuid ->
                currentServiceData[LIST_NAME] =
                    SampleGattAttributes.lookup(uuid = uuid, defaultName = unknownServiceString)
                currentServiceData[LIST_UUID] = uuid

                //gattServiceData += currentServiceData //TODO S

                // print current Service Data
                countServise++
                for ((key, value) in currentServiceData) {
                    println("$countServise  ::> GATT Service  $key  $value")
                }
            }

            // Loops through available Characteristics.
            val charas = arrayListOf<BluetoothGattCharacteristic>()
            //val gattCharacteristicGroupData = arrayListOf<HashMap<String, String>>() //TODO C
            listGattCharacteristics = mutableListOf()
            gattService?.characteristics?.forEach { chara ->
                charas += chara
                val currentCharaData = hashMapOf<String, String>()

                val uuid = chara.uuid.toString()

                currentCharaData[LIST_NAME] =
                    SampleGattAttributes.lookup(uuid = uuid, defaultName = unknownCharaString)
                currentCharaData[LIST_UUID] = uuid

                //gattCharacteristicGroupData += currentCharaData //TODO C

                // print current Characteristic Data
                for ((key, value) in currentCharaData) {
                    println("\t::> Characteristic    $key  $value")
                }
                val props: Int = chara.properties

//                if ((props and ))

                println("\t::> props ($props): ${
                    when(props) {
                        BluetoothGattCharacteristic.PROPERTY_BROADCAST -> {
//                                println(":> PROPERTY_BROADCAST")
                            "PROPERTY_BROADCAST"
                        }
                        BluetoothGattCharacteristic.PROPERTY_READ -> {
//                                println(":> PROPERTY_READ")
                            "PROPERTY_READ"
                        }
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE -> {
//                                println(":> PROPERTY_WRITE_NO_RESPONSE")
                            "PROPERTY_WRITE_NO_RESPONSE"
                        }
                        BluetoothGattCharacteristic.PROPERTY_WRITE -> {
//                                println(":> PROPERTY_WRITE")
                            "PROPERTY_WRITE"
                        }
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY -> {
//                                println(":> PROPERTY_NOTIFY")
                            "PROPERTY_NOTIFY"
                        }
                        BluetoothGattCharacteristic.PROPERTY_INDICATE -> {
//                                println(":> PROPERTY_INDICATE")
                            "PROPERTY_INDICATE"
                        }
                        BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE -> {
//                                println(":> PROPERTY_SIGNED_WRITE")
                            "PROPERTY_SIGNED_WRITE"
                        }
                        BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS -> {
//                                println(":> PROPERTY_EXTENDED_PROPS")
                            "PROPERTY_EXTENDED_PROPS"
                        }
                        else -> "Unknown"
                    }
                }")

                /*// find the necessary characteristics for handle
                when(uuid) { //TODO-1
                    SampleGattAttributes.ST_UUID_CHARACTERISTIC_1,
                    SampleGattAttributes.ST_UUID_CHARACTERISTIC_2,
                    SampleGattAttributes.UUID_CHARACTERISTIC_SERIAL_NUMBER_STRING,
                    -> {
                        println(":> FOUND CHARACTERISTIC: ${
                            SampleGattAttributes.lookup(chara.uuid.toString(), unknownCharaString)
                        }")
                        callbacks?.handleFoundCharacteristic(chara)
                    }
                }*/
                callbacks?.handleFoundCharacteristic(chara)
            }
            listGattCharacteristics += charas
            //gattCharacteristicData += gattCharacteristicGroupData //TODO C
        }
    }
}