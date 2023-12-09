package com.imrul.doorlock.data.lock

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.contentColorFor
import com.imrul.doorlock.domain.lock.BluetoothController
import com.imrul.doorlock.domain.lock.BluetoothDevice
import com.imrul.doorlock.domain.lock.BluetoothDeviceDomain
import com.imrul.doorlock.domain.lock.ConnectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOError
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.math.log


@SuppressLint("MissingPermission")
class AndroidBluetoothController(private val context: Context) : BluetoothController {

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDevice>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDevice>>
        get() = _pairedDevices.asStateFlow()

    private val _isConnected = MutableStateFlow<Boolean>(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val bluetoothManager by lazy { context.getSystemService(BluetoothManager::class.java) }
    private val bluetoothAdapter by lazy { bluetoothManager?.adapter }

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, device ->
        if (bluetoothAdapter?.bondedDevices?.contains(device) == true) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.tryEmit("Can't connect to a non-paired device.")
            }
        }
    }

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    private lateinit var outputStream: OutputStream
    private lateinit var inputStream: InputStream

    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver, IntentFilter().apply {
                addAction(
                    BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED
                )
                addAction(
                    android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED
                )
                addAction(
                    android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED
                )
            }
        )
    }

    override fun sendToHC05(valToSend: String) {
        try {
            // Check if the BluetoothSocket is connected
            if (currentClientSocket?.isConnected == true) {
                inputStream = currentClientSocket!!.inputStream
                outputStream = currentClientSocket!!.outputStream
                // Convert the string to bytes and send
                outputStream.write(valToSend.toByteArray())
                Thread.sleep(1000)
                outputStream.flush()

            } else {
                Toast.makeText(context, "Not Connected to any device", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun lockDoor() {
        sendToHC05("1")
    }

    override fun unlockDoor() {
        sendToHC05("0")
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
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow<ConnectionResult> {
            if (!hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            currentServerSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                "lock_service",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch (e: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServerSocket?.close()
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)

            currentClientSocket = bluetoothDevice
                ?.createInsecureRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )


            stopDiscovery()
            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    delay(1000)
                    emit(ConnectionResult.ConnectionEstablished)
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("HC 05 is not connected"))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentServerSocket = null
        currentClientSocket = null
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

    companion object {
        const val SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }

}