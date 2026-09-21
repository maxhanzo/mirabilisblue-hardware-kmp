package com.mirabilisblue.hardware.domain.error

sealed class MirabilisBlueError(message: String) : Exception(message) {
    data object EmptyFile : MirabilisBlueError("The file is empty")
    data class FileTooLarge(val actual: Int, val maximum: Int) : MirabilisBlueError("File is $actual bytes; maximum is $maximum")
    data object TransferInProgress : MirabilisBlueError("A file transfer is already in progress")
    data object TransferCancelled : MirabilisBlueError("File transfer was cancelled")
    data object MalformedPacket : MirabilisBlueError("Malformed file-transfer packet")
    data object NegativeAcknowledgement : MirabilisBlueError("Peripheral rejected the transfer batch")
}
