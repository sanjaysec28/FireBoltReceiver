package com.example.firebolttreceiver

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.firebolttreceiver.ui.theme.FireBolttReceiverTheme
import java.util.UUID

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var scanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private var status by mutableStateOf("Ready to scan")
    private var heartRate by mutableStateOf("--")
    private var spo2 by mutableStateOf("--")

    companion object {
        // Standard Heart Rate Service
        private val HEART_RATE_SERVICE =
            UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")

        // Standard Heart Rate Measurement
        private val HEART_RATE_CHARACTERISTIC =
            UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")

        // Fire-Boltt proprietary SpO₂ service
        private val FEEA_SERVICE =
            UUID.fromString("0000FEEA-0000-1000-8000-00805F9B34FB")

        // Fire-Boltt SpO₂ characteristic discovered with nRF Connect
        private val FEE3_CHARACTERISTIC =
            UUID.fromString("0000FEE3-0000-1000-8000-00805F9B34FB")

        // Client Characteristic Configuration Descriptor
        private val CCCD =
            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

        private const val DEVICE_NAME = "FireBoltt 128"
    }

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted = permissions.values.all { it }

            if (granted) {
                startBleScan()
            } else {
                status = "Bluetooth permissions denied"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        bluetoothAdapter = bluetoothManager.adapter
        scanner = bluetoothAdapter.bluetoothLeScanner

        setContent {
            FireBolttReceiverTheme {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "🔥 Fire-Boltt Receiver",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "Status: $status"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "❤️ Heart Rate",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "$heartRate BPM",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "🫁 SpO₂",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "$spo2 %",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            requestBluetoothPermissions()
                        }
                    ) {
                        Text("Connect Fire-Boltt")
                    }
                }
            }
        }
    }

    private fun requestBluetoothPermissions() {

        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            permissions.add(
                Manifest.permission.BLUETOOTH_SCAN
            )

            permissions.add(
                Manifest.permission.BLUETOOTH_CONNECT
            )

        } else {

            permissions.add(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        permissionLauncher.launch(
            permissions.toTypedArray()
        )
    }

    @SuppressLint("MissingPermission")
    private fun startBleScan() {

        if (!bluetoothAdapter.isEnabled) {
            status = "Please turn Bluetooth ON"
            return
        }

        // Stop any previous scan first
        scanner?.stopScan(scanCallback)

        status = "Scanning for FireBoltt..."

        scanner = bluetoothAdapter.bluetoothLeScanner

        scanner?.startScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {

        @SuppressLint("MissingPermission")
        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {

            val device = result.device

            val name = device.name

            if (name == DEVICE_NAME) {

                status = "Found $DEVICE_NAME"

                scanner?.stopScan(this)

                connectToWatch(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {

            status = "BLE scan failed: $errorCode"
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToWatch(device: BluetoothDevice) {

        status = "Connecting to Fire-Boltt..."

        bluetoothGatt?.close()

        bluetoothGatt = device.connectGatt(
            this,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            statusCode: Int,
            newState: Int
        ) {

            if (newState == BluetoothGatt.STATE_CONNECTED) {

                status = "Connected! Discovering services..."

                gatt.discoverServices()

            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {

                status = "Disconnected"
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            status: Int
        ) {

            if (status != BluetoothGatt.GATT_SUCCESS) {

                this@MainActivity.status =
                    "Service discovery failed"

                return
            }

            this@MainActivity.status =
                "Connected - listening for data"

            // -----------------------------
            // HEART RATE
            // -----------------------------

            val heartRateService =
                gatt.getService(HEART_RATE_SERVICE)

            val heartRateCharacteristic =
                heartRateService?.getCharacteristic(
                    HEART_RATE_CHARACTERISTIC
                )

            if (heartRateCharacteristic != null) {

                enableNotifications(
                    gatt,
                    heartRateCharacteristic
                )
            }

            // -----------------------------
            // SPO2
            // -----------------------------

            val spo2Service =
                gatt.getService(FEEA_SERVICE)

            val spo2Characteristic =
                spo2Service?.getCharacteristic(
                    FEE3_CHARACTERISTIC
                )

            if (spo2Characteristic != null) {

                enableNotifications(
                    gatt,
                    spo2Characteristic
                )
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {

            val data = characteristic.value

            when (characteristic.uuid) {

                HEART_RATE_CHARACTERISTIC -> {

                    val bpm = parseHeartRate(data)

                    if (bpm > 0) {
                        heartRate = bpm.toString()
                    }
                }

                FEE3_CHARACTERISTIC -> {

                    val value = parseSpo2(data)

                    if (value in 70..100) {
                        spo2 = value.toString()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {

        gatt.setCharacteristicNotification(
            characteristic,
            true
        )

        val descriptor =
            characteristic.getDescriptor(CCCD)

        if (descriptor != null) {

            descriptor.value =
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

            gatt.writeDescriptor(descriptor)
        }
    }

    private fun parseHeartRate(
        data: ByteArray
    ): Int {

        if (data.isEmpty()) return 0

        val flags = data[0].toInt()

        // Bit 0:
        // 0 = UINT8 heart rate
        // 1 = UINT16 heart rate

        return if ((flags and 0x01) == 0) {

            if (data.size >= 2) {
                data[1].toInt() and 0xFF
            } else {
                0
            }

        } else {

            if (data.size >= 3) {

                ((data[2].toInt() and 0xFF) shl 8) or
                        (data[1].toInt() and 0xFF)

            } else {
                0
            }
        }
    }

    private fun parseSpo2(
        data: ByteArray
    ): Int {

        if (data.isEmpty()) return 0

        // Our reverse-engineered Fire-Boltt packet:
        //
        // FE EA 20 06 6B XX
        //
        // XX = SpO₂ percentage

        return data.last().toInt() and 0xFF
    }

    override fun onDestroy() {

        super.onDestroy()

        try {
            scanner?.stopScan(scanCallback)
        } catch (_: Exception) {
        }

        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}