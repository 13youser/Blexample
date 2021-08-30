package com.example.blexample.utils


object SampleGattAttributes {

    const val UUID_SERVICE_HEART_RATE =                         "0000180d-0000-1000-8000-00805f9b34fb"
    const val UUID_SERVICE_DEVICE_INFORMATION =                 "0000180a-0000-1000-8000-00805f9b34fb"
    const val UUID_SERVICE_ALERT_NOTIFICATION =                 "00001811-0000-1000-8000-00805f9b34fb"
    const val UUID_SERVICE_IMMEDIATE_ALERT =                    "00001802-0000-1000-8000-00805f9b34fb"
    const val UUID_SERVICE_BATTERY =                            "0000180f-0000-1000-8000-00805f9b34fb"
    const val UUID_SERVICE_HUMAN_INTERFACE_DEVICE =             "00001812-0000-1000-8000-00805f9b34fb"
    const val UUID_SERVICE_GENERIC_ACCESS =                     "00001800-0000-1000-8000-00805f9b34fb"
    const val UUID_SERVICE_GENERIC_ATTRIBUTE =                  "00001801-0000-1000-8000-00805f9b34fb"

    const val UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT =      "00002a37-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_SERIAL_NUMBER_STRING =        "00002a25-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_DEVICE_NAME =                 "00002a00-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_APPEARANCE =                  "00002a01-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_PP_CONNECTION_PARAMETERS =    "00002a04-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_SERVICE_CHANGED =             "00002a05-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_CLIENT_SUPPORTED_FEATURES =   "00002b29-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_DATABASE_HASH =               "00002b2a-0000-1000-8000-00805f9b34fb"

    const val UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIG =    "00002902-0000-1000-8000-00805f9b34fb"

    // ST microchip
    const val ST_UUID_SERVICE =                                 "d973f2e0-b19e-11e2-9e96-0800200c9a66"
    /**  */
    const val ST_UUID_CHARACTERISTIC_1 =                        "d973f2e1-b19e-11e2-9e96-0800200c9a66"
    /** write */
    const val ST_UUID_CHARACTERISTIC_2 =                        "d973f2e2-b19e-11e2-9e96-0800200c9a66"

    // ?
    const val UUID_SERVICE_CONNECTING_UUID =                    "f000c0c0-0451-4000-b000-000000000000"
    const val UUID_CHARACTERISTIC_WRITE_UUID =                  "f000c0c1-0451-4000-b000-000000000000"
    const val UUID_CHARACTERISTIC_READ_UUID =                   "f000c0c2-0451-4000-b000-000000000000"

    private val attributes = hashMapOf<String?, String?>(
        UUID_SERVICE_HEART_RATE to "Heart Rate",
        UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT to "Heart Rate Measurement",
        UUID_CHARACTERISTIC_SERIAL_NUMBER_STRING to "Serial Number String",
        UUID_CHARACTERISTIC_DEVICE_NAME to "Device Name",
        UUID_CHARACTERISTIC_APPEARANCE to "Appearance",
        UUID_CHARACTERISTIC_PP_CONNECTION_PARAMETERS to "Peripheral Preferred Connection Parameters",
        UUID_CHARACTERISTIC_SERVICE_CHANGED to "Service Changed",
        UUID_CHARACTERISTIC_CLIENT_SUPPORTED_FEATURES to "Client Supported Features",
        UUID_CHARACTERISTIC_DATABASE_HASH to "Database Hash",
        UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIG to "Client Characteristic Configuration",
        UUID_SERVICE_DEVICE_INFORMATION to "Device Information",
        UUID_SERVICE_ALERT_NOTIFICATION to "Alert Notification",
        UUID_SERVICE_IMMEDIATE_ALERT to "Immediate Alert",
        UUID_SERVICE_BATTERY to "Battery",
        UUID_SERVICE_HUMAN_INTERFACE_DEVICE to "Human Interface Device",
        UUID_SERVICE_GENERIC_ACCESS to "Generic Access",
        UUID_SERVICE_GENERIC_ATTRIBUTE to "Generic Attribute",
        "0000fee1-0000-1000-8000-00805f9b34fb" to "Anhui Huami Information Technology Co., Ltd.",
        "0000fee0-0000-1000-8000-00805f9b34fb" to "Anhui Huami Information Technology Co., Ltd.",

        // ?
        UUID_SERVICE_CONNECTING_UUID to "Service Connecting UUID",
        UUID_CHARACTERISTIC_WRITE_UUID to "Characteristic Write UUID",
        UUID_CHARACTERISTIC_READ_UUID to "Characteristic Read UUID",

        // UUID of STMicroelectronics
        ST_UUID_SERVICE to "Service 128bits UUID",
        ST_UUID_CHARACTERISTIC_1 to "Characteristic_1 128bits UUID",
        ST_UUID_CHARACTERISTIC_2 to "Characteristic_2 128bits UUID",
    )

    /** Find name */
    fun lookup(uuid: String, defaultName: String): String {
        val name = attributes[uuid]
        return name ?: defaultName
    }
}