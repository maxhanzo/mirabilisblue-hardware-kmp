package com.mirabilisblue.hardware.client.mock

import com.mirabilisblue.hardware.ble.model.state.BleDeviceState
import com.mirabilisblue.hardware.ble.model.type.BleWriteType
import com.mirabilisblue.hardware.client.BleClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

class MockClient(private val name: String, private val identifier: String) : BleClient {
    private val connected = MutableStateFlow(false)
    private val stateFlow = MutableStateFlow(BleDeviceState.DISCONNECTED)
    override fun getName() = name
    override fun getIdentifier() = identifier
    override fun getAdvertisementData(): ByteArray? = null
    override val state: Flow<BleDeviceState> = stateFlow
    override var currentState: BleDeviceState = BleDeviceState.DISCONNECTED
    override fun isConnected(): StateFlow<Boolean> = connected
    override fun observeCharacteristic(characteristic: String?, onSubscription: (() -> Unit)?): Flow<ByteArray> { onSubscription?.invoke(); return emptyFlow() }
    override fun observeCharacteristic(characteristic: String?): Flow<ByteArray> = emptyFlow()
    override suspend fun writeCharacteristic(characteristic: String?, byteArray: ByteArray, writeType: BleWriteType) = true
    override suspend fun readCharacteristic(characteristic: String?): ByteArray? = null
    override suspend fun safeObserverCharacteristic(characteristic: String?, isSuccess: (ByteArray) -> Boolean) = Unit
    override suspend fun safeWriteCharacteristic(characteristic: String?, byteArray: ByteArray, writeType: BleWriteType) = Unit
    override suspend fun safeReadCharacteristic(characteristic: String?) = Unit
    override suspend fun connect() { currentState = BleDeviceState.CONNECTED; connected.value = true; stateFlow.value = currentState }
    override suspend fun disconnect() { currentState = BleDeviceState.DISCONNECTED; connected.value = false; stateFlow.value = currentState }
}
