package com.myworkshopy.vdash.repository

import com.myworkshopy.vdash.model.VehicleUiState
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicleState(): Flow<VehicleUiState>
}