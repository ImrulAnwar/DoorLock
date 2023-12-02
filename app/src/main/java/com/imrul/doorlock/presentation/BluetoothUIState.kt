package com.imrul.doorlock.presentation

import com.imrul.doorlock.domain.lock.BluetoothDevice

data class BluetoothUIState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList()
)