package com.mirabilisblue.hardware.ble.config

sealed class BleFilter {
    data class NamePrefix(val prefix: String) : BleFilter()
}
