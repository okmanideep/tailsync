package com.nutomic.syncthingandroid

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.google.android.material.color.DynamicColors
import com.nutomic.syncthingandroid.service.Notifier
import com.nutomic.syncthingandroid.service.QuickSettingsTileService
import com.nutomic.syncthingandroid.service.SyncthingService
import com.nutomic.syncthingandroid.util.Languages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class SyncthingApp : Application() {
    @JvmField
    @Inject
    var mComponent: DaggerComponent? = null

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this)

        super.onCreate()

        DaggerDaggerComponent.builder()
            .syncthingModule(SyncthingModule(this))
            .build()
            .inject(this)

        Languages(this).setLanguage(this)

        // The main point here is to use a VM policy without
        // `detectFileUriExposure`, as that leads to exceptions when e.g.
        // opening the ignores file. And it's enabled by default.
        // We might want to disable `detectAll` and `penaltyLog` on release (non-RC) builds too.
        val policy = VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
        StrictMode.setVmPolicy(policy)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationScope.launch {
                Notifier.serviceState.collect {
                    if (it == SyncthingService.State.ACTIVE || it == SyncthingService.State.STARTING) {
                        // Reset the QuickSettings tile when the service is disabled
                        QuickSettingsTileService.setRunning(true)
                    } else {
                        QuickSettingsTileService.setRunning(false)
                    }
                }
            }
        }
    }

    fun component(): DaggerComponent? {
        return mComponent
    }
}
