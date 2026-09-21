package com.mirabilisblue.hardware.domain.service

import com.mirabilisblue.hardware.ble.BleService
import com.mirabilisblue.hardware.client.BleClient
import com.mirabilisblue.hardware.client.BleScan
import com.mirabilisblue.hardware.client.ClientProvider
import com.mirabilisblue.hardware.domain.config.MirabilisBlueConfig
import com.mirabilisblue.hardware.domain.device.MirabilisBlueDevice

class MirabilisBlueService(private val mock: Boolean = false) : BleService<MirabilisBlueDevice>() {
    override fun getScan(): BleScan = if (mock) ClientProvider.mockScanWith() else ClientProvider.scanWith(MirabilisBlueConfig.bleFilter())
    override fun clientTo(client: BleClient?): MirabilisBlueDevice? = client?.let(::MirabilisBlueDevice)
}
