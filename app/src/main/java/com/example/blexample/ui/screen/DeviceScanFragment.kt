package com.example.blexample.ui.screen

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.blexample.R
import com.example.blexample.databinding.FragmentDeviceScanBinding
import com.example.blexample.ui.adapter.LeDeviceListAdapter
import com.example.blexample.ui.base.BaseFragment
import com.example.blexample.ui.viewmodel.DeviceScanViewModel
import com.incotex.mercurycashbox.ui.base.invisible
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Scanning BLE devices screen
 */
class DeviceScanFragment : BaseFragment() {

    private var _binding: FragmentDeviceScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel by sharedViewModel<DeviceScanViewModel>()

    private var leDeviceListAdapter: LeDeviceListAdapter? = null

    companion object {
        fun newInstance() = DeviceScanFragment()

        const val REQUEST_ENABLE_BT = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopScanLeDevice()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.bluetoothAdapter?.isEnabled == false)
            requestBluetoothEnable()

        initView()
        observeEvents()
    }

    private fun initView() {
        leDeviceListAdapter = LeDeviceListAdapter(context = requireContext())
        binding.listView.adapter = leDeviceListAdapter

        binding.buttonScanControl.setOnClickListener {
            when(viewModel.scanningCalled.value) {
                true -> binding.buttonScanControl.text = getString(R.string.scan)
                false -> binding.buttonScanControl.text = getString(R.string.stop)
            }
            viewModel.switchScanLeDevice()
        }
    }

    private fun observeEvents() {
        viewModel.leScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)

                Log.i(TAG, ":>> onScanResult :: device_name=${result.device.name}")
//                println(":>> onScanResult :: device_name=${result.device.name}")

                leDeviceListAdapter?.add(device = result.device)
            }
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)

                Log.e(TAG, ":>> onScanFailed :: errorCode=$errorCode")
//                println(":>> onScanFailed :: errorCode=$errorCode")
            }
        }
        viewModel.scanningCalled.observe(viewLifecycleOwner, { scanning ->
            view?.post {
                binding.buttonScanControl.text = getString(
                    if (scanning)
                        R.string.stop
                    else
                        R.string.scan
                )
                binding.progressScanning.invisible(!scanning)
            }

        })
    }

    private fun requestBluetoothEnable() {
        startActivityForResult(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
            REQUEST_ENABLE_BT
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_ENABLE_BT -> {
                when(resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        Log.d(TAG, ":> Enabling Bluetooth succeeds")
//                        println(":> Enabling Bluetooth succeeds")
                    }
                    AppCompatActivity.RESULT_CANCELED -> {
                        Log.d(TAG, ":> Bluetooth was not enabled due to an error " +
                                "(or the user responded \"Deny\")")
//                        println(":> Bluetooth was not enabled due to an error " +
//                                "(or the user responded \"Deny\")")
//                        requestBluetoothEnable()
                    }
                }
            }
            else -> {
                Log.e(TAG, ":> Unknown request code")
//                println(":> Unknown request code")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}