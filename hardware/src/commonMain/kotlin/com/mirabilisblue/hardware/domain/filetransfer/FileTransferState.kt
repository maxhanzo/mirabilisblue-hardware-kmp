package com.mirabilisblue.hardware.domain.filetransfer

sealed interface FileTransferState {
    data object Idle : FileTransferState
    data class Uploading(val bytesTransferred: Int, val totalBytes: Int) : FileTransferState
    data class Downloading(val bytesTransferred: Int) : FileTransferState
    data class Completed(val data: ByteArray? = null) : FileTransferState
    data object Cancelled : FileTransferState
    data class Failed(val message: String) : FileTransferState
}
