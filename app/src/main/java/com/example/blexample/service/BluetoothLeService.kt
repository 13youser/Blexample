package com.example.blexample.service

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.blexample.R
import com.example.blexample.utils.DisposableManager
import com.example.blexample.utils.SampleGattAttributes
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

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
        const val ACTION_RESULT_CHARA_READ =
            "com.example.blexample.bluetooth.le.ACTION_RESULT_CHARA_READ"
        const val ACTION_RESULT_CHARA_WRITE =
            "com.example.blexample.bluetooth.le.ACTION_RESULT_CHARA_WRITE"
        const val ACTION_RESULT_CHARA_NOTIFICATION =
            "com.example.blexample.bluetooth.le.ACTION_RESULT_CHARA_NOTIFICATION"

        const val EXTRA_DEVICE_CONNECTED = "EXTRA_DEVICE_CONNECTED"
        const val EXTRA_CHARACTERISTIC = "EXTRA_CHARACTERISTIC"
    }

    private val disposableManager = DisposableManager()

    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    var currentDevice: BluetoothDevice? = null
    private var isRepeatRead = false
    private var isRepeatWrite = false

    override fun onBind(intent: Intent): IBinder = binder
    override fun onUnbind(intent: Intent?): Boolean {
        disposableManager.clear()
        disconnect()
        return super.onUnbind(intent)
    }

    /**
     * When the activity unbinds from the service, the connection must be closed to avoid draining
     * the device battery.
     */
    fun disconnect() {
        stopRead()
        stopWrite()
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
                currentDevice = it.getRemoteDevice(address)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
                return false
            }
            bluetoothGatt = currentDevice?.connectGatt(this, false, gattCallback)
            return bluetoothGatt != null
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
        }
    }

    /** Get all the supported services and characteristics in GATT server of connected Device  */
    fun getSupportedGattServices(): List<BluetoothGattService?>? =
        bluetoothGatt?.services

    /*fun subscribeNotifications(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.let { gatt ->

            val name = SampleGattAttributes.lookup(
                uuid = characteristic.uuid.toString(),
                defaultName = resources.getString(R.string.unknown_characteristic)
            )

            disposableManager.add(
                Observable
                    .fromCallable {
                        gatt.setCharacteristicNotification(characteristic, true)



//                        characteristic.value = byteArrayOf(0x01)
//                        gatt.writeCharacteristic(characteristic)
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { Log.d(TAG, ":::+NOTIFICATION Characteristic::> " +
                                "${if (it) "SUCCESS" else "FAILED"} ($name)")
                        },
                        { Log.e(TAG, ":::-NOTIFICATION Characteristic::> " +
                                "THROWABLE ($name): $it")
                        }
                    )
            )
        }
    }*/

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, repeat: Boolean = false) { //TODO-3 write
        bluetoothGatt?.let { gatt ->
            if (repeat) isRepeatWrite = true

            val name = SampleGattAttributes.lookup(
                uuid = characteristic.uuid.toString(),
                defaultName = resources.getString(R.string.unknown_characteristic)
            )

            disposableManager.add(
                Observable
                    .fromCallable { gatt.writeCharacteristic(characteristic) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .repeatUntil { !isRepeatWrite }
                    .subscribe(
                        { Log.d(TAG, ":::+WRITE Characteristic::> " +
                                "${if (it) "SUCCESS" else "FAILED"} ($name)")
                        },
                        { Log.e(TAG, ":::-WRITE Characteristic::> " +
                                "THROWABLE ($name): $it")
                        }
                    )
            )
        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized on WRITE:> chara")
        }
    }

    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        bluetoothGatt?.let { gatt ->
            gatt.setCharacteristicNotification(characteristic, enabled)

            if (SampleGattAttributes.UUID_CHARACTERISTIC_INCOTEX_WS_SCALES_02 == characteristic.uuid.toString()) {
                val descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIG)
                )
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized")
        }
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic, repeat: Boolean = false) {
        bluetoothGatt?.let { gatt ->
            if (repeat) isRepeatRead = true

            val name = SampleGattAttributes.lookup(
                uuid = characteristic.uuid.toString(),
                defaultName = resources.getString(R.string.unknown_characteristic)
            )

            disposableManager.add(
                Observable
                    .fromCallable {

                        gatt.readCharacteristic(characteristic)

                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .repeatUntil { !isRepeatRead }
                    .subscribe(
                        { Log.d(TAG, ":::+READ Characteristic::> " +
                                "${if (it) "SUCCESS" else "FAILED"} ($name)")
                        },
                        { Log.e(TAG, ":::-READ Characteristic::> " +
                                    "THROWABLE ($name): $it")
                        }
                    )
            )
        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized on READ:> chara")
        }
    }

    fun stopRead() { isRepeatRead = false }
    fun stopWrite() { isRepeatWrite = false }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when(newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // successfully connected to the GATT Server
                    broadcast(action = ACTION_GATT_CONNECTED, device = currentDevice)
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
                    broadcast(action = ACTION_RESULT_CHARA_READ, characteristic = characteristic)
                else ->
                    Log.w(TAG, "onCharacteristicRead received: $status")
            }
        }
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            when(status) {
                BluetoothGatt.GATT_SUCCESS ->
                    broadcast(action = ACTION_RESULT_CHARA_WRITE, characteristic = characteristic)
                else ->
                    Log.w(TAG, "onCharacteristicWrite received: $status")
            }
        }
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            broadcast(action = ACTION_RESULT_CHARA_NOTIFICATION, characteristic = characteristic)
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
                    // For all other profiles, writes the data
                    val data = chara.value
                    if (data.isNotEmpty())
                        intent.putExtra(EXTRA_CHARACTERISTIC, data)
                    else
                        Log.w(TAG, "Data empty...")
                }
            }
        }
        sendBroadcast(intent)
    }


}