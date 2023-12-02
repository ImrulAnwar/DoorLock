package com.imrul.doorlock.domain.lock

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name: String,
    val address: String
)
