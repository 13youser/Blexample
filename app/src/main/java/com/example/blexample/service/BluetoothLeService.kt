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
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.blexample.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"

        const val EXTRA_DEVICE_CONNECTED = "EXTRA_DEVICE_CONNECTED"
    }

    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    var connectedDevice: BluetoothDevice? = null

    override fun onBind(intent: Intent): IBinder = binder
    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    /**
     * When the activity unbinds from the service, the connection must be closed to avoid draining
     * the device battery.
     */
    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }

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

    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return bluetoothGatt?.services
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when(newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // successfully connected to the GATT Server
                    broadcast(action = ACTION_GATT_CONNECTED, device = connectedDevice)
                    // Attempts to discover services after successful connection.
                    bluetoothGatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    // disconnected from the GATT Server
                    broadcast(action = ACTION_GATT_DISCONNECTED)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when(status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcast(action = ACTION_GATT_SERVICES_DISCOVERED)
                }
                else -> {
                    Log.w(TAG, "onServicesDiscovered received: $status")
                }
            }
        }
    }

    private fun broadcast(action: String, device: BluetoothDevice? = null) {
        val intent = Intent(action)
        device?.let { intent.putExtra(EXTRA_DEVICE_CONNECTED, it) }
        sendBroadcast(intent)
    }
}