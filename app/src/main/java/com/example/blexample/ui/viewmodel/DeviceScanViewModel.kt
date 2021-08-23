package com.example.blexample.ui.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.blexample.ui.base.BaseViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

class DeviceScanViewModel: BaseViewModel() {

    private companion object {
        const val SCAN_PERIOD: Long = 13000
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

    var leScanCallback: ScanCallback? = null

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
     * Check Bluetooth
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
}