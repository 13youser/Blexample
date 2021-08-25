package com.example.blexample.utils

import java.util.HashMap

object SampleGattAttributes {
    private val attributes = hashMapOf<String?, String?>(
        "00002a37-0000-1000-8000-00805f9b34fb" to "Heart Rate Service",
        "00002902-0000-1000-8000-00805f9b34fb" to "Client Characteristic Config",

        "0000180a-0000-1000-8000-00805f9b34fb" to "Device Information",
        "00001811-0000-1000-8000-00805f9b34fb" to "Alert Notification",
        "00001802-0000-1000-8000-00805f9b34fb" to "Immediate Alert",
        "0000180d-0000-1000-8000-00805f9b34fb" to "Heart Rate",
        "0000fee0-0000-1000-8000-00805f9b34fb" to "Anhui Huami Information Technology Co., Ltd.",
        "0000fee1-0000-1000-8000-00805f9b34fb" to "Anhui Huami Information Technology Co., Ltd.",
        "0000180f-0000-1000-8000-00805f9b34fb" to "Battery",
        "00001812-0000-1000-8000-00805f9b34fb" to "Human Interface Device",
        "00001800-0000-1000-8000-00805f9b34fb" to "Generic Access",
        "00001801-0000-1000-8000-00805f9b34fb" to "Generic Attribute",

        "f000c0c0-0451-4000-b000-000000000000" to "Service Connecting UUID",
        "f000c0c1-0451-4000-b000-000000000000" to "Characteristic Write UUID",
        "f000c0c2-0451-4000-b000-000000000000" to "Characteristic Read UUID",
    )

    fun lookup(uuid: String, defaultName: String): String {
        val name = attributes[uuid]
        return name ?: defaultName
    }
}