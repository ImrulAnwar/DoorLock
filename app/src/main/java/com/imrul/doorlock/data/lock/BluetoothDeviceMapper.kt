package com.imrul.doorlock.data.lock

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.imrul.doorlock.domain.lock.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain():BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}