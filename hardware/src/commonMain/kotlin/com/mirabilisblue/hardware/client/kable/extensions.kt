package com.mirabilisblue.hardware.client.kable

import com.mirabilisblue.hardware.ble.config.BleFilter
import com.mirabilisblue.hardware.ble.model.type.BleWriteType
import com.mirabilisblue.hardware.ble.model.type.BleWriteType.*
import com.juul.kable.Filter
import com.juul.kable.WriteType

fun BleWriteType.toWriteType() = when (this) {
    WithResponse -> WriteType.WithResponse
    WithoutResponse -> WriteType.WithoutResponse
}

fun BleFilter.toFilter() = when (this) {
    is BleFilter.NamePrefix -> Filter.NamePrefix(this.prefix)
}
