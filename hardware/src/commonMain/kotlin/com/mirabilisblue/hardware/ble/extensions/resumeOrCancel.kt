package com.mirabilisblue.hardware.ble.extensions

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

fun <TYPE> CancellableContinuation<TYPE>.resumeWhenActive(value: TYPE) {
    if (this.isActive) this.resume(value)
}

fun <TYPE> CancellableContinuation<TYPE>.cancelWhenActive(value: Throwable?) {
    if (this.isActive) this.cancel(value)
}
