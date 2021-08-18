package com.example.blexample.ui.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.blexample.R

class LeDeviceListAdapter(
    private val context: Context,
) : BaseAdapter() {

    companion object {
        const val ITEM_LAYOUT = R.layout.item_list
    }

    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val listDevice = arrayListOf<BluetoothDevice>()

    override fun getCount(): Int = 0
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItem(position: Int): Any = listDevice[position]

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val v = inflater.inflate(ITEM_LAYOUT, parent, false)
        if (listDevice.isNotEmpty()) {
            val device = listDevice[position]
            v.findViewById<TextView>(R.id.textItemName).text = device.name
            v.findViewById<TextView>(R.id.textItemAddress).text = device.address
        }
        return v
    }

    fun add(device: BluetoothDevice?) {
        device?.let {
            listDevice.add(it)
            notifyDataSetChanged()
        }
    }

    fun clear() {
        listDevice.clear()
        notifyDataSetChanged()
    }

}