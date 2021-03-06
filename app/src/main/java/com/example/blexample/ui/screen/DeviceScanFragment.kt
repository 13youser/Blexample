package com.example.blexample.ui.screen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.blexample.R
import com.example.blexample.databinding.FragmentDeviceScanBinding
import com.example.blexample.ui.adapter.LeDeviceListAdapter
import com.example.blexample.ui.base.BaseFragment
import com.example.blexample.ui.viewmodel.DeviceViewModel
import com.incotex.mercurycashbox.ui.base.invisible
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import android.content.pm.PackageManager
import android.os.Build

import androidx.core.app.ActivityCompat
import android.location.LocationManager
import androidx.appcompat.app.AlertDialog
import com.example.blexample.utils.Utils


/**
 * Scanning BLE devices screen
 */
class DeviceScanFragment : BaseFragment() {

    companion object {
        fun newInstance() = DeviceScanFragment()

        const val REQUEST_ENABLE_BT = 100
        const val REQUEST_PERMISSIONS_BT = 200
        const val REQUEST_PERMISSIONS_GPS = 300
    }

    private var _binding: FragmentDeviceScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel by sharedViewModel<DeviceViewModel>()
    private var leDeviceListAdapter: LeDeviceListAdapter? = null

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
        initView()
        observeEvents()
        requestRequirements()

        viewModel.switchScanLeDevice()
    }

    private fun initView() {

        leDeviceListAdapter = LeDeviceListAdapter(
            onClick = { d ->
                viewModel.tryConnect(device = d)
                navigateBack()
            }
        )

        binding.listRecycler.adapter = leDeviceListAdapter

        binding.buttonScanControl.setOnClickListener {
            val hasPerm = requestRequirements()
            if (!hasPerm) return@setOnClickListener

            when(viewModel.scanningCalled.value) {
                true -> binding.buttonScanControl.text = getString(R.string.scan)
                false -> binding.buttonScanControl.text = getString(R.string.stop)
            }
            viewModel.switchScanLeDevice()
        }
    }

    private fun observeEvents() {
        with(viewModel) {
            scanningCalled.observe(viewLifecycleOwner, { scanning ->
                if (scanning) leDeviceListAdapter?.clear()
                view?.post {
                    with(binding) {
                        buttonScanControl.text = getString(
                            if (scanning)
                                R.string.stop
                            else
                                R.string.scan
                        )
                        textScanning.invisible(!scanning)
                        progressScanning.invisible(!scanning)
                    }
                }
            })
            scanResultDevice.observe(viewLifecycleOwner, {
                leDeviceListAdapter?.add(device = it)
            })
        }

        leDeviceListAdapter?.liveCount?.observe(viewLifecycleOwner, {
            binding.textCountFound.text = it.toString()
        })
    }

    private fun requestRequirements() = requestGeoEnabled() and checkLocationPermissions() and
            checkBluetoothPermissions() and requestBluetoothEnable()

    private fun requestBluetoothEnable():Boolean {
        viewModel.isBluetoothEnabled()?.let { enabled ->
            if (!enabled)
                startActivityForResult(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT
                )
            return enabled
        }
        Utils.showOkDialog(requireContext(),
            getString(R.string.title_oops), getString(R.string.msg_oops))
        return false
    }

    private fun requestGeoEnabled(): Boolean {
        val mLocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val mIsGPSEnabled =
            mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val mIsNetworkEnabled =
            mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val isGeoEnabled = mIsGPSEnabled //&& mIsNetworkEnabled
        if (!isGeoEnabled)
            showMessageMustEnableGeo()

        return isGeoEnabled
    }

    private fun showMessageMustEnableGeo() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.title_permission))
        builder.setMessage(getString(R.string.msg_enable_geo))
        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.dismiss()
            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun checkLocationPermissions(): Boolean {
        val listRequestPermission: MutableList<String> = ArrayList()

        val afl = Manifest.permission.ACCESS_FINE_LOCATION
        val accessFineLocation: Int = requireContext().checkSelfPermission(afl)
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED)
            listRequestPermission.add(afl)

        val acl = Manifest.permission.ACCESS_COARSE_LOCATION
        val accessCoarseLocation: Int = requireContext().checkSelfPermission(acl)
        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED)
            listRequestPermission.add(acl)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // for Android 10+
            val abl = Manifest.permission.ACCESS_BACKGROUND_LOCATION
            val accessBackgroundLocation = requireContext().checkSelfPermission(abl)
            if (accessBackgroundLocation != PackageManager.PERMISSION_GRANTED)
                listRequestPermission.add(abl)
        }

        if (listRequestPermission.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                listRequestPermission.toTypedArray(),
                REQUEST_PERMISSIONS_GPS
            )
            return false
        } else
            return true
    }

    private fun checkBluetoothPermissions(): Boolean {
        val b = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.BLUETOOTH
        )
        if (b != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.BLUETOOTH),
                REQUEST_PERMISSIONS_BT
            )
            return false
        } else
            return true
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_ENABLE_BT -> {
                when(resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        Log.d(TAG, "BLT RESULT_OK :: Enabling Bluetooth succeeds")
                    }
                    AppCompatActivity.RESULT_CANCELED -> {
                        Log.e(TAG, "BLT RESULT_CANCELED Bluetooth was not enabled" +
                                "due to an error (or the user responded \"Deny\")")
                    }
                }
            }
            else -> Log.e(TAG, ":> Unknown request code")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }*/

}