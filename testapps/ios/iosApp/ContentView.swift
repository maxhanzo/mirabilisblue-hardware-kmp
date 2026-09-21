import SwiftUI
import hardware

struct ContentView: View {
   // private let service = MirabilisBlueService(mock: false)
    var body: some View {
        VStack(spacing: 16) {
            Text("Mirabilis Blue KMP")
                .font(.title2)
            Text("BLE-MIRABILIS-BLUE")
                .font(.caption)
           // Button("Start Scan") { service.startScan(timeout: 30_000) }
           // Button("Stop Scan") { service.stopScan() }
        }
        .padding()
    }
}
