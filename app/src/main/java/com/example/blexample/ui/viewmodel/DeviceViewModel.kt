package com.example.blexample.ui.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.blexample.data.DeviceData
import com.example.blexample.data.Preferences
import com.example.blexample.ui.base.BaseViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

class DeviceViewModel(
    private val prefs: Preferences
) : BaseViewModel() {

    private companion object {
        const val SCAN_PERIOD: Long = 13000
    }
    interface LeCallbacks {
        fun connect(device: BluetoothDevice)
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
    var leCallbacks: LeCallbacks? = null

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
        currentDeviceData = DeviceData(device.name, device.address, device.uuids)
        leCallbacks?.connect(device)
    }
}