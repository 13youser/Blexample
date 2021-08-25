package com.example.blexample.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.blexample.R
import com.example.blexample.data.DeviceData
import com.example.blexample.databinding.FragmentMainBinding
import com.example.blexample.ui.base.BaseFragment
import com.example.blexample.ui.viewmodel.DeviceViewModel
import com.incotex.mercurycashbox.ui.base.invisible
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MainFragment : BaseFragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel by sharedViewModel<DeviceViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeEvents()
    }

    private fun initView() {
        /*updateUI(viewModel.currentDeviceData)*/

        with(binding) {
            buttonForget.setOnClickListener {
                viewModel.currentDeviceData = null
                //TODO disconnect device and forget
            }
            buttonSearchNew.setOnClickListener {
                it.navigateAction(R.id.action_mainFragment_to_listFragment)
            }
        }
    }

    private fun observeEvents() {
        viewModel.currentDeviceLiveData.observe(viewLifecycleOwner, {
            updateUI(it)
        })
    }

    private fun updateUI(data: DeviceData?) {
        println(":> update ui -- device $data")
        with(binding) {
            val isDeviceConnected = data?.let {
                textCurrentDeviceName.text = it.name
                textCurrentDeviceAddress.text = it.address
            } != null
            textMessage.invisible(isDeviceConnected)
            frameCurrentDevice.invisible(!isDeviceConnected)
        }
    }
}