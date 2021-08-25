package com.example.blexample.utils

import java.util.HashMap

object SampleGattAttributes {
    private val attributes = hashMapOf<String?, String?>(
        "00002a37-0000-1000-8000-00805f9b34fb" to "Heart Rate Service",
        "00002902-0000-1000-8000-00805f9b34fb" to "Client Characteristic Config",
        //TODO try
        "f000c0c0-0451-4000-b000-000000000000" to "Service Connecting UUID",
        "f000c0c1-0451-4000-b000-000000000000" to "Characteristic Write UUID",
        "f000c0c2-0451-4000-b000-000000000000" to "Characteristic Read UUID",
    )

    fun lookup(uuid: String, defaultName: String): String {
        val name = attributes[uuid]
        return name ?: defaultName
    }
}