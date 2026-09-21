// swift-tools-version:5.9
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://maven.pkg.github.com/mirabilisblue/mirabilisblue-hardware-kmp/com/mirabilisblue/hardware-kmmbridge/0.1.17/hardware-kmmbridge-0.1.17.zip"
let remoteKotlinChecksum = "155d0964ccb94bf68806d50de54eeb046f4565a82ac82d7de152d0e3779d1c4d"
let packageName = "hardware"
// END KMMBRIDGE BLOCK

let package = Package(
    name: packageName,
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: packageName,
            targets: ["TargetWrapper"]
        ),
    ],
    dependencies: [
        .package(url: "https://github.com/rickclephas/KMP-NativeCoroutines.git", exact: "1.0.0-ALPHA-25-spm-no-deps")
    ],
    targets: [
        .target(
            name: "TargetWrapper",
            dependencies: [
                .byName(name: packageName),
                // Swift Concurrency implementation
                .product(name: "KMPNativeCoroutinesAsync", package: "KMP-NativeCoroutines"),
                // Combine implementation
                .product(name: "KMPNativeCoroutinesCombine", package: "KMP-NativeCoroutines"),
            ],
            path: "./TargetWrapper"
        ),
        .binaryTarget(
            name: packageName,
            url: remoteKotlinUrl,
            checksum: remoteKotlinChecksum
        )
    ]
)
