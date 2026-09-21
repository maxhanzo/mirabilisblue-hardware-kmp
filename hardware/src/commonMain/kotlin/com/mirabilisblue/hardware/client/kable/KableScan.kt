package com.mirabilisblue.hardware.client.kable

import com.benasher44.uuid.uuidFrom
import com.mirabilisblue.hardware.ble.config.BleFilter
import com.mirabilisblue.hardware.ble.model.state.BleScanState
import com.mirabilisblue.hardware.client.BleClient
import com.mirabilisblue.hardware.client.BleScan
import com.mirabilisblue.hardware.client.ClientProvider.clientFrom
import com.mirabilisblue.hardware.log.logDebug
import com.mirabilisblue.hardware.log.logError
import com.mirabilisblue.hardware.log.logInfo
import com.mirabilisblue.hardware.scope.ProvideScope.ioScope
import com.juul.kable.Filter
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import com.juul.kable.peripheral
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class KableScan(
    private val filters: List<BleFilter>,
    private val advertisementDataService: String? = null
) : BleScan {

    private var job: Job? = null
    private fun getFilters(): List<Filter.NamePrefix> = filters.map { it.toFilter() }

    /**
     * Bluetooth state
     */
    private val _bleState = MutableStateFlow(BleScanState.IDLE)
    override fun getState(): StateFlow<BleScanState> = _bleState

    private val _clients = MutableStateFlow<BleClient?>(null)
    override fun getClients(): StateFlow<BleClient?> = _clients

    private fun getPeripherals(): Flow<Pair<Peripheral, ByteArray?>> =
        Scanner {
            filters = getFilters()
            logging {
                engine = SystemLogEngine
                level = Logging.Level.Warnings
                format = Logging.Format.Multiline
            }
        }.advertisements
            .onStart {
                _bleState.emit(BleScanState.SCANNING_ON)
                logInfo("Scanning for peripheral")
            }
            .map {
                val data = if (advertisementDataService == null) null
                else it.serviceData(uuidFrom(advertisementDataService))

                Pair(ioScope.peripheral(it) {
                    observationExceptionHandler { cause ->
                        _bleState.emit(BleScanState.ERROR)
                        logDebug("Kable Ble exception: $cause")
                    }
                }, data)
            }
            .onEach {
                /**
                 * Check peripheral? NULL?
                 */
                if (it.first.name != null) {
                    logInfo("Peripheral name: ${it.first.name} id: ${it.first.identifier}")
                    _bleState.emit(BleScanState.DEVICE_FOUND)
                    val client = clientFrom(it)
                    logInfo("Ble client: $client")
                    _clients.emit(client)
                    stopScan()
                }
            }
            .catch {
                _bleState.emit(BleScanState.ERROR)
                logError("Exception: ${it.message}")
            }


    override fun startScan(timeout: Long) {
        ioScope.launch {
            job = getPeripherals().launchIn(ioScope)
            if (timeout > 0L) {
                delay(timeout)
                stopScan()
            }
        }
    }

    override fun stopScan() {
        ioScope.launch { _bleState.emit(BleScanState.SCANNING_OFF) }
        job?.cancel()
    }

    override fun flush() {
        ioScope.launch {
            job?.cancel()
            _bleState.emit(BleScanState.IDLE)
            _clients.emit(null)
        }
    }
}
