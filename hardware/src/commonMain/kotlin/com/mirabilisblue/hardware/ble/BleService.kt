package com.mirabilisblue.hardware.ble

import com.mirabilisblue.hardware.ble.config.BleConfig.scanTimeout
import com.mirabilisblue.hardware.ble.model.state.BleScanState
import com.mirabilisblue.hardware.client.BleClient
import com.mirabilisblue.hardware.client.BleScan
import com.mirabilisblue.hardware.extension.emitWhen
import com.mirabilisblue.hardware.scope.ProvideScope.ioScope
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class BleService<DEVICE : BleDevice> {

    /**
     * Jobs
     */
    private var job: Job? = null

    abstract fun getScan(): BleScan
    abstract fun clientTo(client: BleClient?): DEVICE?

    private val bleScan by lazy { getScan() }

    /**
     * Bluetooth state
     */
    @NativeCoroutines
    fun getState(): StateFlow<BleScanState> = bleScan.getState()

    /**
     * Available devices
     */
    private val devices = MutableStateFlow(setOf<DEVICE>())

    @NativeCoroutines
    fun getDevices(): StateFlow<Set<DEVICE>> = devices
    private suspend fun emitDevice(device: DEVICE?) {
        if (device == null) return
        devices.emitWhen(device) { it.getName() != device.getName() }
    }

    /**
     * Scanning
     */
    fun startScan(timeout: Long = scanTimeout) {
        job?.cancel()

        job = bleScan.getClients()
            .map { clientTo(it) }
            .onEach { emitDevice(it) }
            .launchIn(ioScope)

        bleScan.startScan(timeout)
    }

    fun stopScan() = bleScan.stopScan()
    fun flush() {
        ioScope.launch { devices.emit(emptySet()) }
        bleScan.flush()
    }
}
