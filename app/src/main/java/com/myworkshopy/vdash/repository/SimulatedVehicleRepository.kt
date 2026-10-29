package com.myworkshopy.vdash.repository
import com.myworkshopy.vdash.model.RideState
import com.myworkshopy.vdash.model.VehicleUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SimulatedVehicleRepository : VehicleRepository {

    override fun observeVehicleState(): Flow<VehicleUiState> = flow {
        val states = listOf(
            VehicleUiState(
                speed = 0,
                motorOn = false,
                killSwitchActive = false,
                rideState = RideState.PARKED
            ),
            VehicleUiState(
                speed = 0,
                motorOn = true,
                killSwitchActive = false,
                rideState = RideState.READY
            ),
            VehicleUiState(
                speed = 25,
                motorOn = true,
                killSwitchActive = false,
                rideState = RideState.RIDING
            ),
            VehicleUiState(
                speed = 50,
                motorOn = true,
                killSwitchActive = false,
                rideState = RideState.ACCELERATING
            ),
            VehicleUiState(
                speed = 0,
                motorOn = false,
                killSwitchActive = true,
                rideState = RideState.MOTOR_DISABLED
            )
        )

        while (true) {
            for (state in states) {
                emit(state)
                delay(1_000L)
            }
        }
    }
}