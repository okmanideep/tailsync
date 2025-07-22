package com.nutomic.syncthingandroid.service

import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class SyncTileService : TileService() {
    override fun onClick() {
        super.onClick()
        // unlockAndRun
    }

    override fun onStartListening() {
        super.onStartListening()
        // set qsTile
    }

    override fun onStopListening() {
        // unset qsTile
        super.onStopListening()
    }
}