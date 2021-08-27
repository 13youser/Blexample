package com.example.blexample.ui.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.res.Resources
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.blexample.R
import com.example.blexample.data.DeviceData
import com.example.blexample.data.Preferences
import com.example.blexample.ui.base.BaseViewModel
import com.example.blexample.utils.SampleGattAttributes
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

class DeviceViewModel(
    private val prefs: Preferences
) : BaseViewModel() {

    private companion object {
        const val SCAN_PERIOD: Long = 13000

        const val LIST_NAME = "NAME"
        const val LIST_UUID = "uuid"
    }
    interface Callbacks {
        fun connect(device: BluetoothDevice)
        fun singleReadCharacteristic(characteristic: BluetoothGattCharacteristic)
    }
    private val callableStartScanning = Callable<Unit> {
        leScanCallback?.let {
            leScanner?.startScan(it)
        }
    }
    private val callableStopScanning = Callable<Unit> {
        leScanCallback?.let {
            leScanner?.stopScan(it)
        }
    }
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var leScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var currentReadCharacteristic: BluetoothGattCharacteristic? = null
    private var listGattCharacteristics = mutableListOf<ArrayList<BluetoothGattCharacteristic>>()
    private val runnableForPendingStopScanning = Runnable { callStopScanLe() }
    private val handler = Handler()

    private val _scanningCalled = MutableLiveData<Boolean>()
    val scanningCalled: LiveData<Boolean> get() = _scanningCalled

    private val _currentDeviceLiveData = MutableLiveData<DeviceData?>()
    val currentDeviceLiveData: LiveData<DeviceData?> get() = _currentDeviceLiveData

    var currentDeviceData: DeviceData? = null
        get() = _currentDeviceLiveData.value
            ?: prefs.deviceData
                .also { _currentDeviceLiveData.value = it }
        set(value) {
            field = value
            prefs.deviceData = value
            _currentDeviceLiveData.value = value
        }

    init {
        currentDeviceData = prefs.deviceData
    }

    var leScanCallback: ScanCallback? = null
    var callbacks: Callbacks? = null

    private fun callStartScanLe() {
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
        leScanCallback?.let {
            if (_scanningCalled.value != true) { // Stops scanning after a pre-defined scan period.
                callStartScanLe()
            } else {
                callStopScanLe()
            }
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
        currentDeviceData = DeviceData(device.name, device.address)
        callbacks?.connect(device)
    }

    /** Iterate through the supported GATT */
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

                //TODO:: choose characteristic to read
                if (uuid == SampleGattAttributes.ST_UUID_CHARACTERISTIC_1) {
                    println(":> READ CHARACTERISTIC: ${
                        SampleGattAttributes.lookup(chara.uuid.toString(), unknownCharaString)
                    }")
                    currentReadCharacteristic = chara
                    callbacks?.singleReadCharacteristic(chara)
                }

                currentCharaData[LIST_NAME] =
                    SampleGattAttributes.lookup(uuid = uuid, defaultName = unknownCharaString)
                currentCharaData[LIST_UUID] = uuid

                //gattCharacteristicGroupData += currentCharaData //TODO C

                // print current Characteristic Data
                for ((key, value) in currentCharaData) {
                    println("\t::> Characteristic    $key  $value")
                }
            }
            listGattCharacteristics += charas
            //gattCharacteristicData += gattCharacteristicGroupData //TODO C
        }
    }
}