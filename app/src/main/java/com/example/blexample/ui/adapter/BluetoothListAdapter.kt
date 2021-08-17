package com.example.blexample.ui.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.blexample.R
import java.util.ArrayList

class BluetoothListAdapter(
    private val context: Context,
    private val bluetoothDevices: ArrayList<BluetoothDevice>,
) : BaseAdapter() {

    companion object {
        const val ITEM_LAYOUT = R.layout.item_list
    }

    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = 0

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItem(position: Int): Any {
        return Any() //TODO
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val v = inflater.inflate(ITEM_LAYOUT, parent, false)

        return v
    }

}