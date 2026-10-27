package com.myworkshopy.vdash.model

enum class RideState(val label: String) {
    PARKED("PARKED"),
    READY("READY"),
    RIDING("RIDING"),
    ACCELERATING("ACCELERATING"),
    MOTOR_DISABLED("MOTOR DISABLED")
}

enum class SignalStatus { WAITING, LIVE, STALE, UNAVAILABLE }

data class VehicleUiState(
    val motorOn: Boolean = false,
    val killSwitchActive: Boolean = false,
    val speed: Int = 0,
    val rideState: RideState = RideState.PARKED,
    val signalStatus: SignalStatus = SignalStatus.LIVE,
    val speedWasInvalid: Boolean = false
)
