import SwiftUI
import hardware

@main
struct iOSApp: App {
    
    init() {
        LogProvider.shared.setup()
    }
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
