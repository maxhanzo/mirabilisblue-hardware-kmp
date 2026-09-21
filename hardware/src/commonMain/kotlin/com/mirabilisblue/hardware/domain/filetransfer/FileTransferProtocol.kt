package com.mirabilisblue.hardware.domain.filetransfer

object FileTransferProtocol {
    const val maximumFileSize = 16_384
    const val maximumPacketSize = 20
    const val maximumPayloadSize = 18
    const val acknowledgementInterval = 8

    const val START: Byte = 0x02
    const val END: Byte = 0x03
    const val ACK: Byte = 0x06
    const val NACK: Byte = 0x15
    const val CANCEL: Byte = 0x18
    const val UPLOAD: Byte = 0x55
    const val DOWNLOAD: Byte = 0x44

    data class Chunk(val marker: Byte, val sequence: UByte, val payload: ByteArray) {
        fun encode(): ByteArray = byteArrayOf(marker, sequence.toByte()) + payload
    }

    fun uploadCommand(size: Int): ByteArray {
        require(size in 1..maximumFileSize)
        return byteArrayOf(
            UPLOAD,
            size.toByte(),
            (size shr 8).toByte(),
            (size shr 16).toByte(),
            (size shr 24).toByte()
        )
    }

    fun chunks(data: ByteArray): List<Chunk> {
        if (data.isEmpty()) return emptyList()
        var offset = 0
        var sequence: UByte = 0u
        return buildList {
            while (offset < data.size) {
                val end = minOf(offset + maximumPayloadSize, data.size)
                add(Chunk(if (end == data.size) END else START, sequence, data.copyOfRange(offset, end)))
                offset = end
                sequence = (sequence + 1u).toUByte()
            }
        }
    }
}
