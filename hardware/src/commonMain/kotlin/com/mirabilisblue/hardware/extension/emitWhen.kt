package com.mirabilisblue.hardware.extension

import kotlinx.coroutines.flow.MutableStateFlow

suspend fun <T> MutableStateFlow<Set<T>>.emitWhen(toAdd: T, predicate: (T) -> Boolean) {
    val item = value.find { predicate(it) }
    if (item != null) return

    this.emit(value + toAdd)
}