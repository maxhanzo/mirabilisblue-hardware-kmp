package com.mirabilisblue.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mirabilisblue.hardware.domain.service.MirabilisBlueService

class MainActivity : ComponentActivity() {
    private val service = MirabilisBlueService()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        service.startScan()
    }
    override fun onDestroy() {
        service.stopScan()
        super.onDestroy()
    }
}
