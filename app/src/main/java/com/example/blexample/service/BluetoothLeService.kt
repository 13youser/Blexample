package com.example.blexample.service

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class BluetoothLeService : Service() {

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService = this@BluetoothLeService
    }

    companion object {
        const val TAG = "BluetoothLeService"

        const val ACTION_GATT_CONNECTED =
            "com.example.blexample.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.blexample.bluetooth.le.ACTION_GATT_DISCONNECTED"
    }

    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    var connectedDevice: BluetoothDevice? = null

    override fun onBind(intent: Intent): IBinder = binder

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    /**
     * Connect to BLE device by MAC-address
     *
     * @return true if success
     * */
    fun connect(address: String): Boolean {
        bluetoothAdapter?.let {
            try {
                connectedDevice = it.getRemoteDevice(address)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
                return false
            }
            bluetoothGatt = connectedDevice?.connectGatt(this, false, gattCallback)
            return bluetoothGatt != null
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when(newState) {
                BluetoothProfile.STATE_CONNECTED ->
                    broadcast(action = ACTION_GATT_CONNECTED)
                BluetoothProfile.STATE_DISCONNECTED ->
                    broadcast(action = ACTION_GATT_DISCONNECTED)
            }
        }
    }

    private fun broadcast(action: String) { //todo: onreceive in proadcast (see activity)
        sendBroadcast(Intent(action))
    }
}