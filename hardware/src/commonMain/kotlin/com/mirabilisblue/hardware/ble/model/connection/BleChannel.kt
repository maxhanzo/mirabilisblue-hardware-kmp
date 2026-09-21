package com.mirabilisblue.hardware.ble.model.connection

internal class BleChannel<out ENTITY>(
    val value: ENTITY,
    val status: Boolean
) {
    inline val isSuccess get() = status
}
