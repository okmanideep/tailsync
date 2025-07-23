package com.nutomic.syncthingandroid.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object Notifier {
    private val _serviceState = MutableStateFlow(SyncthingService.State.DISABLED)
    val serviceState: StateFlow<SyncthingService.State> = _serviceState

    @JvmStatic
    fun onStateChanged(state: SyncthingService.State) {
        _serviceState.value = state
    }
}