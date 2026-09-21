package com.mirabilisblue.hardware.ble.extensions

import com.mirabilisblue.hardware.ble.error.BleError
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Characteristic
import com.juul.kable.DiscoveredService
import com.juul.kable.Service
import kotlinx.coroutines.Job

internal fun List<DiscoveredService>.getCharacteristicBy(characteristicUUID: String?): Characteristic? {
    if (characteristicUUID == null) return null

    return try {
        val uuid: Uuid = uuidFrom(characteristicUUID)
        forEach { service ->
            service.characteristics.forEach { characteristic ->
                if (characteristic.characteristicUuid == uuid) return characteristic
            }
        }

        null
    } catch (t: Throwable) {
        null
    }
}

internal fun List<DiscoveredService>.getServiceBy(serviceUUID: String?): Service? {
    if (serviceUUID == null) return null

    return try {
        val uuid: Uuid = uuidFrom(serviceUUID)
        find { it.serviceUuid == uuid }
    } catch (t: Throwable) {
        null
    }
}

fun Boolean.checkBleOperation() {
    if (!this) throw BleError.OperationFailed()
}

fun Job.addToList(jobs: ArrayList<Job>): Job {
    jobs.add(this)
    return this
}

fun Long.toMinimalByteArray(): ByteArray {
    if (this == 0L) return byteArrayOf(0)

    val result = mutableListOf<Byte>()
    var value = this

    while (value != 0L) {
        result.add((value and 0xFF).toByte())
        value = value ushr 8
    }

    return result.toByteArray()
}