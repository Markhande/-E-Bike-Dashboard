package com.myworkshopy.vdash.di

import com.myworkshopy.vdash.repository.SimulatedVehicleRepository
import com.myworkshopy.vdash.repository.VehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VehicleModule {

    @Provides
    @Singleton
    fun provideVehicleRepository(): VehicleRepository {
        return SimulatedVehicleRepository()
    }

}