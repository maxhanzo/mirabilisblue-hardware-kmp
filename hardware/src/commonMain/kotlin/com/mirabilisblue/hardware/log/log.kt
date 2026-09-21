package com.mirabilisblue.hardware.log

import io.github.aakira.napier.Napier

fun logDebug(value: String) = Napier.d(value)
fun logInfo(value: String) = Napier.i(value)
fun logError(value: String) = Napier.e(value)
