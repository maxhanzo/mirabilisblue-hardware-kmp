package com.mirabilisblue.hardware.client

import com.mirabilisblue.hardware.ble.model.state.BleDeviceState
import com.mirabilisblue.hardware.ble.model.type.BleWriteType
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BleClient {
    fun getName(): String
    fun getIdentifier(): String
    fun getAdvertisementData(): ByteArray?

    @NativeCoroutines
    val state: Flow<BleDeviceState>
    var currentState: BleDeviceState

    @NativeCoroutines
    fun isConnected(): StateFlow<Boolean>

    @NativeCoroutines
    fun observeCharacteristic(
        characteristic: String?,
        onSubscription: (() -> Unit)?
    ): Flow<ByteArray>

    @NativeCoroutines
    fun observeCharacteristic(characteristic: String?): Flow<ByteArray>

    @NativeCoroutines
    suspend fun writeCharacteristic(
        characteristic: String?,
        byteArray: ByteArray,
        writeType: BleWriteType = BleWriteType.WithoutResponse
    ): Boolean

    @NativeCoroutines
    suspend fun readCharacteristic(characteristic: String?): ByteArray?

    /**
     * With Mutex
     */
    @NativeCoroutines
    suspend fun safeObserverCharacteristic(
        characteristic: String?,
        isSuccess: (response: ByteArray) -> Boolean
    )

    /**
     * With Mutex
     */
    @NativeCoroutines
    suspend fun safeWriteCharacteristic(
        characteristic: String?,
        byteArray: ByteArray,
        writeType: BleWriteType = BleWriteType.WithoutResponse
    )

    /**
     * With Mutex
     */
    @NativeCoroutines
    suspend fun safeReadCharacteristic(characteristic: String?)

    @NativeCoroutines
    suspend fun connect()

    @NativeCoroutines
    suspend fun disconnect()
}
