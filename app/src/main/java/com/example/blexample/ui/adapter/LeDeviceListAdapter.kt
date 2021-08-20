package com.example.blexample.ui.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.blexample.R

class LeDeviceListAdapter(
    private val onClick: (BluetoothDevice) -> Unit
) : ListAdapter<BluetoothDevice, LeDeviceListAdapter.DeviceViewHolder>(DeviceDiffCallback) {

    companion object {
        const val ITEM_LAYOUT = R.layout.item_list
    }

    private val listDevice = mutableListOf<BluetoothDevice>()

    var count = 0

    fun add(device: BluetoothDevice) {
        if (!listDevice.contains(device)) {
            count++
            println(":> qweqqqqqqqqqqqqqqq :: $count")
            listDevice.add(device)
            submitList(listDevice)
        }
    }

    /*private val listDevice = mutableListOf<BluetoothDevice>()

    fun addAll(list: List<BluetoothDevice>) {
        listDevice.addAll(list)
        submitList(listDevice)
    }*/

    class DeviceViewHolder(
        itemView: View, val onClick: (BluetoothDevice) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private var textName = itemView.findViewById<TextView>(R.id.textItemName)
        private var textAddress = itemView.findViewById<TextView>(R.id.textItemAddress)

        private var device: BluetoothDevice? = null

        init {
            itemView.setOnClickListener {
                device?.let {
                    onClick(it)
                }
            }
        }

        /* Bind device name and address  */
        fun bind(device: BluetoothDevice) {
            this.device = device
            textName.text = device.name ?: "no_name"
            textAddress.text = device.address
        }
    }

    /* Creates and inflates view and return DeviceViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DeviceViewHolder(
        itemView = LayoutInflater.from(parent.context)
            .inflate(ITEM_LAYOUT, parent, false),
        onClick = onClick
    )

    /* Gets current device and uses it to bind view. */
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object DeviceDiffCallback : DiffUtil.ItemCallback<BluetoothDevice>() {
    override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice) =
        oldItem == newItem
    override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice) =
        oldItem.uuids
            .contentEquals(
                newItem.uuids
            )
}