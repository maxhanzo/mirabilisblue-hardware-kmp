package com.mirabilisblue.hardware.client

import com.mirabilisblue.hardware.ble.model.state.BleScanState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BleScan {
    @NativeCoroutines
    fun getState(): StateFlow<BleScanState>

    fun startScan(timeout: Long)
    fun stopScan()
    fun flush()

    @NativeCoroutines
    fun getClients(): Flow<BleClient?>
}
