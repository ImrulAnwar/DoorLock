package com.imrul.doorlock.data.lock

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.imrul.doorlock.domain.lock.BluetoothController
import com.imrul.doorlock.domain.lock.BluetoothDevice
import com.imrul.doorlock.domain.lock.BluetoothDeviceDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.log


@SuppressLint("MissingPermission")
class AndroidBluetoothController(private val context: Context) : BluetoothController {

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())

    override val scannedDevices: StateFlow<List<BluetoothDevice>>
        get() = _scannedDevices.asStateFlow()
    override val pairedDevices: StateFlow<List<BluetoothDevice>>
        get() = _pairedDevices.asStateFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }

    }

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }


    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    init {
        updatePairedDevices()
    }

    override fun startDiscovery() {
        if (!hasPermission((android.Manifest.permission.BLUETOOTH_SCAN))) {
            return
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND)
        )
        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if (!hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
    }

    private fun updatePairedDevices() {

        Log.d(TAG, "updatePairedDevices: hello")
        if (bluetoothManager == null || !hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }

        val adapter = bluetoothAdapter
        if (adapter != null) {
            try {
                val bondedDevices = adapter.bondedDevices
                val devices = bondedDevices.map { it.toBluetoothDeviceDomain() }
                _pairedDevices.update { devices }
            } catch (e: Exception) {
                // Log the exception
                e.printStackTrace()
            }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

}