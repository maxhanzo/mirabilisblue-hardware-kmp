package com.mirabilisblue.hardware.ble

import com.mirabilisblue.hardware.ble.model.state.BleDeviceState
import com.mirabilisblue.hardware.client.BleClient
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

abstract class BleDevice(private val client: BleClient, @Suppress("UNUSED_PARAMETER") liveInfo: Boolean = false) {
    internal fun getClient() = client
    fun getName() = client.getName()
    fun getIdentifier() = client.getIdentifier()
    fun getCurrentState() = client.currentState

    @NativeCoroutines fun getState(): Flow<BleDeviceState> = client.state
    @NativeCoroutines fun isConnected(): StateFlow<Boolean> = client.isConnected()
    @NativeCoroutines suspend fun connect() = client.connect()
    @NativeCoroutines suspend fun disconnect() { if (client.isConnected().value) client.disconnect() }
}
