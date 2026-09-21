package com.mirabilisblue.hardware.ble.error

import com.juul.kable.Identifier

sealed interface BleError {

    data object ConnectionClosed : Throwable(message = "Ble Connection closed error!"), BleError
    object Unknown : Throwable(message = "Ble unknown error!"), BleError
    class CharacteristicNotFound(
        characteristic: String?,
        throwable: Throwable? = null
    ) :
        Throwable(
            message = "Ble Characteristic $characteristic not found error! Cause: $throwable",
            cause = throwable
        ), BleError

    class OperationFailed(throwable: Throwable? = null) :
        Throwable(
            message = "Ble operation failed message error!",
            cause = throwable
        ), BleError

    class ReadOperation(characteristic: String, throwable: Throwable) :
        Throwable(
            message = "Ble read operation error! Characteristic: $characteristic Cause: $throwable",
            cause = throwable
        ), BleError

    class UnknownConnection(throwable: Throwable) :
        Throwable(
            message = "Ble unknown connection error! Cause: $throwable",
            cause = throwable
        ), BleError

    class WriteCharacteristic(
        characteristic: String?,
        byteArray: ByteArray,
        throwable: Throwable? = null
    ) : Throwable(
        message = "Ble write operation error! Was not possible to write $byteArray value into characteristic: $characteristic",
        cause = throwable
    ), BleError

    class ReadCharacteristic(
        characteristic: String?,
        throwable: Throwable
    ) : Throwable(
        message = "Ble read characteristic error! Characteristic: $characteristic Cause: $throwable",
        cause = throwable
    ), BleError

    class ObserveCharacteristic(
        characteristic: String?,
        throwable: Throwable
    ) : Throwable(
        message = "Ble observe characteristic error! Characteristic: $characteristic Cause: $throwable",
        cause = throwable
    ), BleError

    class ServiceNotFound(service: String?) :
        Throwable(message = "Ble service not found error! Service:$service"), BleError

    class SafeWriteCharacteristic(
        characteristic: String?,
        throwable: Throwable
    ) : Throwable(
        message = "Ble safe write characteristic error! Characteristic:$characteristic Cause:$throwable",
        cause = throwable
    ), BleError

    class SafeReadCharacteristic(
        characteristic: String?,
        throwable: Throwable
    ) : Throwable(
        message = "Ble safe read characteristic error! Characteristic:$characteristic Cause:$throwable",
        cause = throwable
    ), BleError

    class Connection(identifier: Identifier, throwable: Throwable) : Throwable(
        message = "Ble connection error! Device:$identifier Cause:$throwable",
        cause = throwable
    ), BleError

    class Disconnection(identifier: Identifier, throwable: Throwable) : Throwable(
        message = "Ble disconnection error! Device:$identifier Cause:$throwable",
        cause = throwable
    ), BleError

    class ReadBattery(throwable: Throwable) : Throwable(
        message = "Ble read battery error! Cause:$throwable",
        cause = throwable
    ), BleError

    class ReadFirmware(throwable: Throwable) : Throwable(
        message = "Ble read firmware error! Cause:$throwable",
        cause = throwable
    ), BleError

    class ReadSoftware(throwable: Throwable) : Throwable(
        message = "Ble read software error! Cause:$throwable",
        cause = throwable
    ), BleError

    class ReadManufacture(throwable: Throwable) : Throwable(
        message = "Ble read manufacture error! Cause:$throwable",
        cause = throwable
    ), BleError

    class ReadModelNumber(throwable: Throwable) : Throwable(
        message = "Ble read model number error! Cause:$throwable",
        cause = throwable
    ), BleError

    class ReadSerial(throwable: Throwable) : Throwable(
        message = "Ble read serial error! Cause:$throwable",
        cause = throwable
    ), BleError

    class ReadHardware(throwable: Throwable) : Throwable(
        message = "Ble read hardware error! Cause:$throwable",
        cause = throwable
    ), BleError
}

