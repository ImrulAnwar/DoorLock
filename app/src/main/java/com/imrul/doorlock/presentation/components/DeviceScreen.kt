package com.imrul.doorlock.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imrul.doorlock.domain.lock.BluetoothDevice
import com.imrul.doorlock.presentation.BluetoothUIState
import com.imrul.doorlock.ui.theme.fontFamily

@Composable
fun DeviceScreen(
    state: BluetoothUIState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClicked: (BluetoothDevice) -> Unit,
    onStartServer: () -> Unit,
    onLockDoor: () -> Unit,
    onUnlockDoor: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "DoorLock",
            fontWeight = FontWeight.Bold, fontSize = 30.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            color = Color(0xFF49688D),
        )
        BluetoothDeviceList(
            pairedDevices = state.pairedDevices,
            onClick = onDeviceClicked,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onStartScan
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 20.dp, 20.dp, 60.dp)
                .background(
                    color = Color(0xFFFFFFFF), // Set your desired background color
                    shape = RoundedCornerShape(20.dp) // Set the shape to make the corners round
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text(
                    text = "Tap to Lock/Unlock",
                    fontWeight = FontWeight.Bold, fontSize = 20.sp,
                    modifier = Modifier.padding(20.dp),
                    color = Color(0xFF49688D)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 0.dp, 0.dp, 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onLockDoor,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6F7F9)),
                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 15.dp)
                    ) {
                        Text(text = "Lock Door", color = Color(0xFF49688D), fontFamily = fontFamily)
                    }
                    Button(
                        onClick = onUnlockDoor,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6F7F9)),
                        modifier = Modifier.padding(15.dp, 0.dp, 0.dp, 0.dp)
                    ) {
                        Text(
                            text = "Unlock Door",
                            color = Color(0xFF49688D),
                            fontFamily = fontFamily
                        )
                    }
                }

            }
        }


    }
}

@Composable
fun BluetoothDeviceList(
    pairedDevices: List<BluetoothDevice>,
    onClick: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier,
    onStartScan: () -> Unit
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = "Paired Devices",
                fontWeight = FontWeight.Bold, fontSize = 24.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { onStartScan() },
                color = Color(0xFF49688D),
            )
        }

        item {
            Divider(
                modifier = Modifier.padding(20.dp, 5.dp),
                color = Color(0xFF49688D)
            )
        }
        items(pairedDevices) { device ->
            Text(
                text = device.name,
                color = Color(0xFF49688D),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onClick(device) }
            )
        }
    }

}