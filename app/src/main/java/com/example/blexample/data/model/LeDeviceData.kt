package com.example.blexample.data.model

//import android.os.ParcelUuid

data class LeDeviceData(
    val name: String? = "no_name",
    val address: String? = "",
//    val uuids: Array<ParcelUuid>?
) {
    /*override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LeDeviceData

        if (name != other.name) return false
        if (address != other.address) return false
        if (!uuids.contentEquals(other.uuids)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + uuids.contentHashCode()
        return result
    }*/
}
