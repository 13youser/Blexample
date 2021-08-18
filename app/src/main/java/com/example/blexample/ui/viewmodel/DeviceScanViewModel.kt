package com.example.blexample.ui.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blexample.ui.screen.DeviceScanFragment

class DeviceScanViewModel: ViewModel() {

    companion object {
        // Stops scanning after 10 seconds.
        const val SCAN_PERIOD: Long = 10000
    }

    private val handler = Handler()

    private val _scanningLiveData = MutableLiveData<Boolean>()
    val scanningLiveData: LiveData<Boolean> get() = _scanningLiveData

    var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    var leScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    var leScanCallback: ScanCallback? = null

    fun scanLeDevice() {
        leScanCallback?.let {
            println(":>>> INFO :: value=${_scanningLiveData.value}")
            if (_scanningLiveData.value != true) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    println(":>>> :: FALSE")
                    _scanningLiveData.value = false
                    leScanner?.stopScan(it)
                }, SCAN_PERIOD)
                println(":>>> :: TRUE")
                _scanningLiveData.value = true
                leScanner?.startScan(it)
            } else {
                println(":>>> :: ELSE-FALSE")
                _scanningLiveData.value = false
                leScanner?.stopScan(it)
            }
        }
    }
}