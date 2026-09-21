package com.mirabilisblue.hardware.domain.device

import com.mirabilisblue.hardware.ble.BleDevice
import com.mirabilisblue.hardware.ble.model.type.BleWriteType
import com.mirabilisblue.hardware.client.BleClient
import com.mirabilisblue.hardware.domain.config.MirabilisBlueCharacteristics
import com.mirabilisblue.hardware.domain.error.MirabilisBlueError
import com.mirabilisblue.hardware.domain.filetransfer.FileTransferProtocol
import com.mirabilisblue.hardware.domain.filetransfer.FileTransferState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MirabilisBlueDevice(client: BleClient) : BleDevice(client, liveInfo = false) {
    private val transferState = MutableStateFlow<FileTransferState>(FileTransferState.Idle)
    private var transferJob: Job? = null

    @NativeCoroutines
    fun getFileTransferState(): StateFlow<FileTransferState> = transferState

    @NativeCoroutines
    suspend fun readSerialNumber(): ByteArray? = getClient().readCharacteristic(MirabilisBlueCharacteristics.SERIAL_NUMBER)

    @NativeCoroutines
    suspend fun readHardwareRevision(): ByteArray? = getClient().readCharacteristic(MirabilisBlueCharacteristics.HARDWARE_REVISION)

    @NativeCoroutines
    suspend fun readFirmwareRevision(): ByteArray? = getClient().readCharacteristic(MirabilisBlueCharacteristics.FIRMWARE_REVISION)

    @NativeCoroutines
    suspend fun writeBasic(value: ByteArray): Boolean = getClient().writeCharacteristic(
        MirabilisBlueCharacteristics.BASIC_WRITE, value, BleWriteType.WithResponse
    )

    @NativeCoroutines
    suspend fun readLastWrittenValue(): ByteArray? = getClient().readCharacteristic(MirabilisBlueCharacteristics.LAST_WRITTEN_VALUE)

    @NativeCoroutines
    suspend fun writeObservable(value: ByteArray): Boolean = getClient().writeCharacteristic(
        MirabilisBlueCharacteristics.OBSERVABLE_WRITE, value, BleWriteType.WithResponse
    )

    @NativeCoroutines
    fun observeValue() = getClient().observeCharacteristic(MirabilisBlueCharacteristics.OBSERVABLE_VALUE)

    @NativeCoroutines
    fun observePeriodicEvents() = getClient().observeCharacteristic(MirabilisBlueCharacteristics.PERIODIC_EVENT_STREAM)

    @NativeCoroutines
    suspend fun writeWithoutResponse(value: ByteArray): Boolean = getClient().writeCharacteristic(
        MirabilisBlueCharacteristics.WRITE_WITHOUT_RESPONSE, value, BleWriteType.WithoutResponse
    )

    @NativeCoroutines
    suspend fun readLastWriteWithoutResponseValue(): ByteArray? =
        getClient().readCharacteristic(MirabilisBlueCharacteristics.LAST_WRITE_WITHOUT_RESPONSE_VALUE)

    @NativeCoroutines
    suspend fun secureWrite(value: ByteArray): Boolean = getClient().writeCharacteristic(
        MirabilisBlueCharacteristics.SECURE_WRITE, value, BleWriteType.WithResponse
    )

    @NativeCoroutines
    suspend fun readSecureState(): ByteArray? = getClient().readCharacteristic(MirabilisBlueCharacteristics.SECURE_STATE)

    @NativeCoroutines
    fun observeSecureState() = getClient().observeCharacteristic(MirabilisBlueCharacteristics.SECURE_STATE)

    @NativeCoroutines
    suspend fun readTotalUploadedBytes(): ULong? {
        val bytes = getClient().readCharacteristic(MirabilisBlueCharacteristics.TOTAL_UPLOADED_BYTES) ?: return null
        if (bytes.size < 8) return null
        var value = 0uL
        for (i in 0 until 8) value = value or (bytes[i].toUByte().toULong() shl (8 * i))
        return value
    }

    @NativeCoroutines
    suspend fun upload(data: ByteArray) = coroutineScope {
        if (data.isEmpty()) throw MirabilisBlueError.EmptyFile
        if (data.size > FileTransferProtocol.maximumFileSize) throw MirabilisBlueError.FileTooLarge(data.size, FileTransferProtocol.maximumFileSize)
        if (transferJob?.isActive == true) throw MirabilisBlueError.TransferInProgress

        val responses = Channel<ByteArray>(Channel.UNLIMITED)
        val observer = launch {
            getClient().observeCharacteristic(MirabilisBlueCharacteristics.FILE_TRANSFER_TX).collect { responses.send(it) }
        }
        transferJob = observer
        try {
            transferState.value = FileTransferState.Uploading(0, data.size)
            getClient().writeCharacteristic(
                MirabilisBlueCharacteristics.FILE_TRANSFER_RX,
                FileTransferProtocol.uploadCommand(data.size),
                BleWriteType.WithoutResponse
            )

            val chunks = FileTransferProtocol.chunks(data)
            var sentBytes = 0
            for (batch in chunks.chunked(FileTransferProtocol.acknowledgementInterval)) {
                for (chunk in batch) {
                    getClient().writeCharacteristic(
                        MirabilisBlueCharacteristics.FILE_TRANSFER_RX,
                        chunk.encode(),
                        BleWriteType.WithoutResponse
                    )
                }
                val response = responses.receive()
                when (response.firstOrNull()) {
                    FileTransferProtocol.ACK -> Unit
                    FileTransferProtocol.NACK -> throw MirabilisBlueError.NegativeAcknowledgement
                    FileTransferProtocol.CANCEL -> throw MirabilisBlueError.TransferCancelled
                    else -> throw MirabilisBlueError.MalformedPacket
                }
                sentBytes += batch.sumOf { it.payload.size }
                transferState.value = FileTransferState.Uploading(sentBytes, data.size)
            }
            transferState.value = FileTransferState.Completed()
        } catch (t: Throwable) {
            transferState.value = FileTransferState.Failed(t.message ?: "File transfer failed")
            throw t
        } finally {
            observer.cancel()
            responses.close()
            transferJob = null
        }
    }

    @NativeCoroutines
    suspend fun download(): ByteArray = coroutineScope {
        if (transferJob?.isActive == true) throw MirabilisBlueError.TransferInProgress
        val result = mutableListOf<Byte>()
        val completed = CompletableDeferred<Unit>()
        var expected: UByte = 0u
        var chunksSinceAck = 0

        val observer = launch {
            getClient().observeCharacteristic(MirabilisBlueCharacteristics.FILE_TRANSFER_TX).collect { packet ->
                if (packet.size < 2) return@collect
                val marker = packet[0]
                val sequence = packet[1].toUByte()
                if (marker != FileTransferProtocol.START && marker != FileTransferProtocol.END) return@collect
                if (sequence != expected) {
                    getClient().writeCharacteristic(MirabilisBlueCharacteristics.FILE_TRANSFER_RX, byteArrayOf(FileTransferProtocol.NACK), BleWriteType.WithoutResponse)
                    return@collect
                }
                result.addAll(packet.drop(2))
                expected = (expected + 1u).toUByte()
                chunksSinceAck++
                transferState.value = FileTransferState.Downloading(result.size)
                if (chunksSinceAck == FileTransferProtocol.acknowledgementInterval || marker == FileTransferProtocol.END) {
                    getClient().writeCharacteristic(
                        MirabilisBlueCharacteristics.FILE_TRANSFER_RX,
                        byteArrayOf(FileTransferProtocol.ACK, sequence.toByte()),
                        BleWriteType.WithoutResponse
                    )
                    chunksSinceAck = 0
                }
                if (marker == FileTransferProtocol.END) completed.complete(Unit)
            }
        }
        transferJob = observer
        try {
            transferState.value = FileTransferState.Downloading(0)
            getClient().writeCharacteristic(
                MirabilisBlueCharacteristics.FILE_TRANSFER_RX,
                byteArrayOf(FileTransferProtocol.DOWNLOAD),
                BleWriteType.WithoutResponse
            )
            completed.await()
            result.toByteArray().also { transferState.value = FileTransferState.Completed(it) }
        } catch (t: Throwable) {
            transferState.value = FileTransferState.Failed(t.message ?: "File transfer failed")
            throw t
        } finally {
            observer.cancel()
            transferJob = null
        }
    }

    @NativeCoroutines
    suspend fun cancelFileTransfer() {
        getClient().writeCharacteristic(
            MirabilisBlueCharacteristics.FILE_TRANSFER_RX,
            byteArrayOf(FileTransferProtocol.CANCEL),
            BleWriteType.WithoutResponse
        )
        transferJob?.cancel()
        transferJob = null
        transferState.value = FileTransferState.Cancelled
    }
}
