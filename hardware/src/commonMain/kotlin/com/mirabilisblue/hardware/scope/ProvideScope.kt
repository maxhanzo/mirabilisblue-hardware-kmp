package com.mirabilisblue.hardware.scope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job

object ProvideScope {
    internal val ioScope: CoroutineScope =
        CoroutineScope(Job() + Dispatchers.IO)
}
