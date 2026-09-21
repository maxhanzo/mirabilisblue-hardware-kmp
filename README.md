# Mirabilis Blue KMP + Kable

Kotlin Multiplatform Bluetooth library for the **BLE-MIRABILIS-BLUE** nRF52840/nice!nano v2 tutorial peripheral.

## Supported BLE operations

- Scan for `BLE-MIRABILIS-BLUE`
- Connect/disconnect through Kable
- Device Information reads: serial number, hardware revision, firmware revision
- Basic write + read-back
- Observable write + notifications
- Periodic notifications
- Write Without Response + read-back
- Secure write/read/notify (access triggers the platform BLE security flow when encryption is required)
- Bidirectional file transfer over GATT RX/TX
- Total uploaded bytes (`UInt64`, little-endian)

## Main API

- `MirabilisBlueService` — scanning/discovery
- `MirabilisBlueDevice` — connection and GATT operations
- `FileTransferProtocol` / `FileTransferState` — transfer protocol and state

