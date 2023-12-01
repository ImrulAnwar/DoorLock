package com.imrul.doorlock

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult
import com.imrul.doorlock.ui.theme.DoorLockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoorLockTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LockButton("Lock", { lockDevice() })
                        LockButton("Unlock") { checkIfBluetoothIsEnabled(this@MainActivity) }
                    }
                }
            }
        }
    }
}


fun lockDevice(){

}

fun unlockDevice() {

}

fun checkIfBluetoothIsEnabled(context: Context):Boolean {

    val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    if (bluetoothAdapter == null) {
        Toast.makeText(context, "Device doesn't have bluetooth", Toast.LENGTH_SHORT).show()
        return false
    }else{
        if (bluetoothAdapter.isEnabled) {
            Toast.makeText(context, "Bluetooth is turned ON", Toast.LENGTH_SHORT).show()
            return false
        } else {
            Toast.makeText(context, "Turn ON Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }
    return true
}

@Composable
fun LockButton(text: String, onClick:()->Unit){
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
        modifier = Modifier.size(height = 80.dp, width = 140.dp),
        
    ) {
        Text(
            text = text,
            fontSize = 25.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}