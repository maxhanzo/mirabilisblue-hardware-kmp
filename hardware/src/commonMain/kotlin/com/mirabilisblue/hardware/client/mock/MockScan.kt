package com.mirabilisblue.hardware.client.mock

import com.mirabilisblue.hardware.ble.model.state.BleScanState
import com.mirabilisblue.hardware.client.BleClient
import com.mirabilisblue.hardware.client.BleScan
import com.mirabilisblue.hardware.client.ClientProvider.mockClientFrom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockScan : BleScan {
    private val state = MutableStateFlow(BleScanState.IDLE)
    private val clients = MutableStateFlow<BleClient?>(null)
    override fun getState(): StateFlow<BleScanState> = state
    override fun getClients(): StateFlow<BleClient?> = clients
    override fun startScan(timeout: Long) { state.value = BleScanState.DEVICE_FOUND; clients.value = mockClientFrom("BLE-MIRABILIS-BLUE", "MOCK") }
    override fun stopScan() { state.value = BleScanState.SCANNING_OFF }
    override fun flush() { state.value = BleScanState.IDLE; clients.value = null }
}
