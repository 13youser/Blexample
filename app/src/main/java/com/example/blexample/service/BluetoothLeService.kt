package com.example.blexample.service

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.blexample.utils.SampleGattAttributes

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
        const val ACTION_DATA_AVAILABLE =
            "com.example.blexample.bluetooth.le.ACTION_DATA_AVAILABLE"

        const val EXTRA_DEVICE_CONNECTED = "EXTRA_DEVICE_CONNECTED"
        const val EXTRA_CHARACTERISTIC = "EXTRA_CHARACTERISTIC"
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

    /** Get all the supported services and characteristics in GATT server of connected Device  */
    fun getSupportedGattServices(): List<BluetoothGattService?>? =
        bluetoothGatt?.services


    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.readCharacteristic(characteristic)
            ?: run {
                Log.w(TAG, "BluetoothGatt not initialized")
            }
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
                BluetoothGatt.GATT_SUCCESS ->
                    broadcast(action = ACTION_GATT_SERVICES_DISCOVERED)
                else ->
                    Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            when(status) {
                BluetoothGatt.GATT_SUCCESS ->
                    broadcast(action = ACTION_DATA_AVAILABLE, characteristic = characteristic)
                else ->
                    Log.w(TAG, "onCharacteristicRead received: $status")
            }
        }
    }

    private fun broadcast(
        action: String,
        device: BluetoothDevice? = null,
        characteristic: BluetoothGattCharacteristic? = null
    ) {
        val intent = Intent(action)

        device?.let { intent.putExtra(EXTRA_DEVICE_CONNECTED, it) }

        characteristic?.let { chara ->
            when (chara.uuid.toString()) {
                SampleGattAttributes.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT -> {

                    val flag = chara.properties
                    val format = when (flag and 0x01) {
                        0x01 -> {
                            Log.d(TAG, "Heart rate format UINT16.")
                            BluetoothGattCharacteristic.FORMAT_UINT16
                        }
                        else -> {
                            Log.d(TAG, "Heart rate format UINT8.")
                            BluetoothGattCharacteristic.FORMAT_UINT8
                        }
                    }
                    val heartRate = chara.getIntValue(format, 1)
                    Log.d(TAG, String.format("Received heart rate: %d", heartRate))
                    intent.putExtra(EXTRA_CHARACTERISTIC, (heartRate).toString())
                }
                else -> {
                    // For all other profiles, writes the data formatted in HEX.
                    val data = chara.value
                    if (data.isNotEmpty()) {
                        intent.putExtra(EXTRA_CHARACTERISTIC, data)
                    }
                    else { }
                }
            }
        }

        sendBroadcast(intent)
    }
}