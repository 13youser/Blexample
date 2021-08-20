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
    private var listDevice: ArrayList<BluetoothDevice>,
//    private var mapDevice: MutableMap<>
) : BaseAdapter() {

    override fun getCount(): Int = listDevice.size
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItem(position: Int): BluetoothDevice = listDevice[position]

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val holder: ViewHolder

        var cView = convertView
        if (cView == null) {
            cView = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
            holder = ViewHolder(cView)
            cView?.tag = holder
        } else {
            holder = cView.tag as ViewHolder
        }

        val device = listDevice[position]
        println(":> :::::: device --->  ${device.address}  ${device.name}")
        holder.textName?.text = device.name ?: "<no_name>"
        holder.textAddress?.text = device.address ?: "NULL"

        return cView
    }

    inner class ViewHolder(view: View?) {
        var textName = view?.findViewById<TextView>(R.id.textItemName)
        var textAddress = view?.findViewById<TextView>(R.id.textItemAddress)
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