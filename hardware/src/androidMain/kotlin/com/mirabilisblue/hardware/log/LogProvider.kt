package com.mirabilisblue.hardware.log

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

actual object LogProvider {
    actual fun setup() {
        Napier.base(DebugAntilog())
    }
}