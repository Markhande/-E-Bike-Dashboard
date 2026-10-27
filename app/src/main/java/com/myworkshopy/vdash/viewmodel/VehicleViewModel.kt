package com.myworkshopy.vdash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myworkshopy.vdash.model.SignalStatus
import com.myworkshopy.vdash.model.VehicleUiState
import com.myworkshopy.vdash.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        VehicleUiState(signalStatus = SignalStatus.WAITING)
    )
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()
    private var watchdog: Job? = null

    init {
        viewModelScope.launch {
            armWatchdog(SignalStatus.UNAVAILABLE)
            try {
                repository.observeVehicleState().collect { sample ->
                    // Motor state never forces speed to zero: a vehicle can coast.
                    _uiState.value = sample.copy(
                        speed = sample.speed.coerceAtLeast(0),
                        speedWasInvalid = sample.speed < 0,
                        signalStatus = SignalStatus.LIVE
                    )
                    armWatchdog(SignalStatus.STALE)
                }
                // A finished stream cannot provide live vehicle data.
                markUnavailable()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                markUnavailable()
            }
        }
    }

    private fun armWatchdog(status: SignalStatus) {
        watchdog?.cancel()
        watchdog = viewModelScope.launch {
            delay(STALE_AFTER_MS)
            _uiState.value = _uiState.value.copy(signalStatus = status)
        }
    }

    private fun markUnavailable() {
        watchdog?.cancel()
        _uiState.value = _uiState.value.copy(signalStatus = SignalStatus.UNAVAILABLE)
    }

    companion object {
        // Three missed one-second samples are treated as a lost signal.
        const val STALE_AFTER_MS = 3_000L
    }
}
