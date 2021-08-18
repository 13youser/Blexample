package com.example.blexample.ui.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.blexample.ui.base.BaseViewModel

class DeviceScanViewModel: BaseViewModel() {

    companion object {
        // Stops scanning after 10 seconds.
        const val SCAN_PERIOD: Long = 10000
    }

    private val handler = Handler()

    private val _scanningProgressUI = MutableLiveData<Boolean>()
    val scanningProgressUI: LiveData<Boolean> get() = _scanningProgressUI

    var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    var leScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    var leScanCallback: ScanCallback? = null

    val taskStopScanning = Runnable {
        leScanCallback?.let {
            println(":> scan :: task-FALSE")
            stopScanningLe(it)
        }
    }

    fun scanLeDevice() {
        leScanCallback?.let {
            if (_scanningProgressUI.value != true) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed(taskStopScanning, SCAN_PERIOD)

                startScanningLe(it)
                    .also { println(":> scan :: TRUE") }
            } else {
                stopScanningLe(it)
                    .also { println(":> scan :: ELSE-FALSE") }
            }
        }
    }

    fun stopScanLeDevice() {
        leScanCallback?.let {
            stopScanningLe(it)
                .also { println(":> scan :: stopScanLeDevice-FALSE") }
        }
    }

    private fun startScanningLe(it: ScanCallback) {
        _scanningProgressUI.value = true
        leScanner?.startScan(it)
    }

    private fun stopScanningLe(it: ScanCallback) {
        handler.removeCallbacks(taskStopScanning)

        _scanningProgressUI.value = false
        leScanner?.stopScan(it)
    }
}