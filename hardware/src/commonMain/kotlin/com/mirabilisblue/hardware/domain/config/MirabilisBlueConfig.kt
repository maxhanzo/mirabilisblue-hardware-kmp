package com.mirabilisblue.hardware.domain.config

import com.mirabilisblue.hardware.ble.config.BleFilter

object MirabilisBlueConfig {
    const val advertisedName = "BLE-MIRABILIS-BLUE"
    fun bleFilter() = listOf(BleFilter.NamePrefix(advertisedName))
}
