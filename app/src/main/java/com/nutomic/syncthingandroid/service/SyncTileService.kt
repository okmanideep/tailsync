package com.nutomic.syncthingandroid.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class SyncTileService : TileService(), ServiceConnection, SyncthingService.OnServiceStateChangeListener {
    private var syncthingService: SyncthingService? = null
    private var isListening = false

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        val service = syncthingService
        if (tile.state != Tile.STATE_ACTIVE && service == null) {
            sendBroadcast(Intent("com.nutomic.syncthingandroid.action.START"))
            // bind to service
            val intent = Intent(this, SyncthingService::class.java)
            bindService(intent, this, Context.BIND_AUTO_CREATE)

            tile.state = Tile.STATE_ACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Starting"
            }
            tile.updateTile()
        } else if (tile.state == Tile.STATE_ACTIVE && service != null) {
            sendBroadcast(Intent("com.nutomic.syncthingandroid.action.STOP"))
            updateTileAsInactive()
            syncthingService?.unregisterOnServiceStateChangeListener(this)
            unbindService(this)
            syncthingService = null
        } else {
            // ignore for now
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        isListening = true

        if (syncthingService == null) {
            // bind to service
            val intent = Intent(this, SyncthingService::class.java)
            bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStopListening() {
        isListening = false

        if (syncthingService == null) return

        syncthingService?.unregisterOnServiceStateChangeListener(this)
        unbindService(this)
        syncthingService = null

        super.onStopListening()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        assert(service is SyncthingServiceBinder)

        val binder = service as SyncthingServiceBinder
        binder.service.registerOnServiceStateChangeListener(this)
        syncthingService = binder.service

        onServiceStateChange(binder.service.currentState)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        syncthingService = null
    }

    override fun onNullBinding(name: ComponentName?) {
        super.onNullBinding(name)
    }

    override fun onServiceStateChange(currentState: SyncthingService.State?) {
        when (currentState) {
            SyncthingService.State.ACTIVE -> updateActiveTile()
            SyncthingService.State.DISABLED -> updateTileAsInactive()
            SyncthingService.State.ERROR -> updateTileAsUnavailable()
            else -> return
        }
    }


    private fun updateActiveTile() {
        val service = syncthingService ?: return

        val api = service.api ?: return
        api.getOverallCompletionInfo { info ->
            val tile = qsTile ?: return@getOverallCompletionInfo
            tile.state = Tile.STATE_ACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (info.completion == 1.0) {
                    tile.subtitle = "Idle"
                } else {
                    tile.subtitle = "Syncing ${(info.completion * 100).toInt()}%"
                    // TODO: schedule an update after a second
                }
            }
            tile.updateTile()
        }
    }

    private fun updateTileAsInactive() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_INACTIVE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = ""
        }
        tile.updateTile()
    }

    private fun updateTileAsUnavailable() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_UNAVAILABLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = ""
        }
        tile.updateTile()
    }
}