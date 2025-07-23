package com.nutomic.syncthingandroid.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.nutomic.syncthingandroid.receiver.AppConfigReceiver
import kotlin.jvm.java


@RequiresApi(Build.VERSION_CODES.N)
class QuickSettingsTileService: TileService() {
    companion object {
        private const val TAG = "QuickSettingsTileService"
        private val lock = Any()
        private var isRunning = false
        private var currentTile: Tile? = null

        @JvmStatic
        fun setRunning(running: Boolean) {
            synchronized(lock) {
                isRunning = running
            }
            updateTile()
        }

        fun setCurrentTile(tile: Tile?) {
            synchronized(lock) {
                currentTile = tile
            }
            updateTile()
        }

        fun updateTile() {
            val tile = currentTile
            val running = isRunning

            if (tile == null) {
                return
            }

            tile.state = if (running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (running) "Connected" else "Not Connected"
            }
            tile.updateTile()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        synchronized(lock) {
            setCurrentTile(qsTile)
        }
    }

    override fun onStopListening() {
        synchronized(lock) {
            setCurrentTile(null)
        }
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick();
        unlockAndRun(this::secureClick)
    }

    private fun secureClick() {
        val running = isRunning
        val intent = if (running) {
            Intent(this.applicationContext, AppConfigReceiver::class.java).apply { action = "com.nutomic.syncthingandroid.action.STOP" }
        } else {
            Intent(this.applicationContext, AppConfigReceiver::class.java).apply { action = "com.nutomic.syncthingandroid.action.START" }
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this.applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent.send()
    }
}