package com.mirabilisblue.hardware.client.kable

import com.mirabilisblue.hardware.ble.config.BleConfig.operationTimeout
import com.mirabilisblue.hardware.ble.extensions.checkBleOperation
import com.mirabilisblue.hardware.ble.error.BleError
import com.mirabilisblue.hardware.ble.extensions.getCharacteristicBy
import com.mirabilisblue.hardware.ble.extensions.getServiceBy
import com.mirabilisblue.hardware.ble.model.connection.BleChannel
import com.mirabilisblue.hardware.ble.model.state.BleDeviceState
import com.mirabilisblue.hardware.ble.model.type.BleWriteType
import com.mirabilisblue.hardware.client.BleClient
import com.mirabilisblue.hardware.scope.ProvideScope.ioScope
import com.juul.kable.Characteristic
import com.juul.kable.Peripheral
import com.juul.kable.Service
import com.juul.kable.State
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class KableClient(
    private val peripheral: Peripheral,
    private val advertisementData: ByteArray? = null
) : BleClient {

    override fun getName(): String = peripheral.name ?: "undefined"
    override fun getIdentifier(): String = peripheral.identifier.toString()
    override fun getAdvertisementData(): ByteArray? = advertisementData

    override var currentState: BleDeviceState = BleDeviceState.UNKNOWN

    override val state: Flow<BleDeviceState> = peripheral.state
        .map {
            when (it) {
                State.Connected -> {
                    _isConnected.emit(true)
                    return@map BleDeviceState.CONNECTED
                }

                State.Connecting.Observes,
                State.Connecting.Services,
                State.Connecting.Bluetooth -> BleDeviceState.CONNECTING

                State.Disconnecting -> BleDeviceState.DISCONNECTING
                State.Disconnected() -> {
                    _isConnected.emit(false)
                    return@map BleDeviceState.DISCONNECTED
                }
                else -> BleDeviceState.UNKNOWN
            }
        }
        .onEach { currentState = it }

    private val _isConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override fun isConnected(): StateFlow<Boolean> = _isConnected

    @NativeCoroutines
    override fun observeCharacteristic(characteristic: String?): Flow<ByteArray> =
        observeCharacteristic(characteristic, null)

    @NativeCoroutines
    override fun observeCharacteristic(
        characteristic: String?,
        onSubscription: (() -> Unit)?
    ): Flow<ByteArray> {
        val char: Characteristic = getCharacteristic(characteristic)
        return try {
            if (onSubscription == null) peripheral.observe(char)
            else peripheral.observe(char, onSubscription)
        } catch (throwable: Throwable) {
            throw BleError.ObserveCharacteristic(characteristic, throwable)
        }
    }

    @NativeCoroutines
    override suspend fun writeCharacteristic(
        characteristic: String?,
        byteArray: ByteArray,
        writeType: BleWriteType
    ): Boolean {
        val char: Characteristic = getCharacteristic(characteristic)
        return try {
            peripheral.write(char, byteArray, writeType.toWriteType())
            true
        } catch (throwable: Throwable) {
            throw BleError.WriteCharacteristic(characteristic, byteArray, throwable)
        }
    }

    @NativeCoroutines
    override suspend fun readCharacteristic(characteristic: String?): ByteArray? {
        val char: Characteristic = getCharacteristic(characteristic)
        return try {
            peripheral.read(char)
        } catch (throwable: Throwable) {
            throw BleError.ReadCharacteristic(characteristic, throwable)
        }
    }

    override suspend fun connect() {
        withContext(ioScope.coroutineContext) {
            try {
                peripheral.connect()
            } catch (throwable: Throwable) {
                throw BleError.Connection(peripheral.identifier, throwable)
            }
        }
    }

    override suspend fun disconnect() {
        withContext(ioScope.coroutineContext) {
            try {
                peripheral.disconnect()
                channel.close()
            } catch (throwable: Throwable) {
                throw BleError.Disconnection(peripheral.identifier, throwable)
            }
        }
    }

    private fun getCharacteristic(characteristic: String?): Characteristic {
        val value = peripheral.services?.getCharacteristicBy(characteristic)
            ?: throw BleError.CharacteristicNotFound(characteristic)
        return value
    }

    private fun getService(service: String?): Service {
        val value = peripheral.services?.getServiceBy(service)
            ?: throw BleError.ServiceNotFound(service)
        return value
    }

    /**
     * Ble connection
     */
    private val channel = Channel<BleChannel<Characteristic>>()
    private val bleOperationMutex = Mutex()

    private val _isClosed = MutableStateFlow(false)

    @NativeCoroutines
    fun isClosed(): StateFlow<Boolean> = _isClosed

    private fun checkNotClosed() {
        if (_isClosed.value) throw Exception("Bluetooth connection is closed!")
    }

    private fun <ENTITY> SendChannel<BleChannel<ENTITY>>.sendBleChannel(
        value: ENTITY,
        status: Boolean
    ) {
        ioScope.launch { send(BleChannel(value, status)) }
    }

    @NativeCoroutines
    override suspend fun safeObserverCharacteristic(
        characteristic: String?,
        isSuccess: (response: ByteArray) -> Boolean
    ) {
        ioScope.launch {
            observeCharacteristic(characteristic).collect { response ->
                val status = isSuccess(response)
                channel.sendBleChannel(getCharacteristic(characteristic), status)
                this.cancel()
            }
        }
    }

    @NativeCoroutines
    override suspend fun safeWriteCharacteristic(
        characteristic: String?,
        byteArray: ByteArray,
        writeType: BleWriteType
    ) {
        bleRequest(channel) {
            val char: Characteristic = getCharacteristic(characteristic)
            try {
                write(char, byteArray, writeType.toWriteType())
                true
            } catch (throwable: Throwable) {
                throw BleError.SafeWriteCharacteristic(characteristic, throwable)
            }
        }
    }

    @NativeCoroutines
    override suspend fun safeReadCharacteristic(characteristic: String?) {
        bleRequest(channel) {
            val char: Characteristic = getCharacteristic(characteristic)
            try {
                read(char)
                true
            } catch (throwable: Throwable) {
                throw BleError.SafeReadCharacteristic(characteristic, throwable)
            }
        }
    }

    /**
     * Mutex
     */
    private suspend inline fun <ENTITY> bleRequest(
        channel: ReceiveChannel<BleChannel<ENTITY>>,
        crossinline operation: suspend Peripheral.() -> Boolean
    ): ENTITY {
        checkNotClosed()
        val mutex = bleOperationMutex
        return mutex.queueWithTimeout {
            try {
                checkNotClosed()
                peripheral.operation().checkBleOperation()
                val response = channel.receive()
                if (response.isSuccess) {
                    response.value
                } else {
                    throw BleError.Unknown
                }
            } catch (throwable: Throwable) {
                throw BleError.OperationFailed(throwable)
            }
        }
    }

    private suspend fun <T> Mutex.queueWithTimeout(
        timeout: Long = operationTimeout,
        block: suspend CoroutineScope.() -> T
    ): T =
        try {
            withLock { withTimeout(timeout, block) }
        } catch (throwable: Throwable) {
            throw throwable
        }
}
