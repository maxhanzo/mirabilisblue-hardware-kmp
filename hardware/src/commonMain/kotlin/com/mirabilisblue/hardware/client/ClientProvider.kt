package com.mirabilisblue.hardware.client

import com.mirabilisblue.hardware.ble.config.BleFilter
import com.mirabilisblue.hardware.client.kable.KableClient
import com.mirabilisblue.hardware.client.kable.KableScan
import com.mirabilisblue.hardware.client.mock.MockClient
import com.mirabilisblue.hardware.client.mock.MockScan
import com.juul.kable.Peripheral

object ClientProvider {
    fun mockScanWith(): BleScan = MockScan()
    fun mockClientFrom(name: String, identifier: String): BleClient = MockClient(name, identifier)

    fun clientFrom(
        value: Pair<Peripheral, ByteArray?>
    ): BleClient = KableClient(value.first, value.second)

    fun scanWith(
        filters: List<BleFilter>,
        advertisementDataService: String? = null
    ): BleScan = KableScan(filters, advertisementDataService)
}
