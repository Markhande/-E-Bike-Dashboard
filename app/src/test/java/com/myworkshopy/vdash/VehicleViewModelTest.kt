package com.myworkshopy.vdash

import androidx.lifecycle.ViewModelStore
import com.myworkshopy.vdash.model.RideState
import com.myworkshopy.vdash.model.SignalStatus
import com.myworkshopy.vdash.model.VehicleUiState
import com.myworkshopy.vdash.repository.SimulatedVehicleRepository
import com.myworkshopy.vdash.repository.VehicleRepository
import com.myworkshopy.vdash.viewmodel.VehicleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    // Cancel the long-lived ViewModel before runTest drains its scheduler.
    // Otherwise the simulator's infinite loop keeps scheduling future samples.
    private fun vehicleTest(block: suspend TestScope.() -> Unit) = runTest(dispatcher) {
        try {
            block()
        } finally {
            store.clear()
            runCurrent()
        }
    }

    private fun model(source: Flow<VehicleUiState>): VehicleViewModel =
        model(object : VehicleRepository {
            override fun observeVehicleState() = source
        })

    private fun model(repository: VehicleRepository): VehicleViewModel =
        VehicleViewModel(repository).also { store.put("vehicle", it) }

    @Test
    fun simulationUpdatesAllFiveStatesEverySecondAndLoops() = vehicleTest {
        val vm = model(SimulatedVehicleRepository())
        val expected = listOf(
            VehicleUiState(false, false, 0, RideState.PARKED),
            VehicleUiState(true, false, 0, RideState.READY),
            VehicleUiState(true, false, 25, RideState.RIDING),
            VehicleUiState(true, false, 50, RideState.ACCELERATING),
            VehicleUiState(false, true, 0, RideState.MOTOR_DISABLED)
        )
        runCurrent()
        expected.forEach { sample ->
            assertEquals(sample, vm.uiState.value)
            advanceTimeBy(999)
            runCurrent()
            assertEquals("No early update", sample, vm.uiState.value)
            advanceTimeBy(1)
            runCurrent()
        }
        assertEquals(expected.first(), vm.uiState.value)
    }

    @Test
    fun negativeSpeedIsClampedAndWarningClearsOnValidSample() = vehicleTest {
        val source = MutableSharedFlow<VehicleUiState>(extraBufferCapacity = 1)
        val vm = model(source)
        runCurrent()
        source.emit(VehicleUiState(speed = -25))
        runCurrent()
        assertEquals(0, vm.uiState.value.speed)
        assertTrue(vm.uiState.value.speedWasInvalid)
        assertEquals(SignalStatus.LIVE, vm.uiState.value.signalStatus)
        source.emit(VehicleUiState(speed = 0))
        runCurrent()
        assertEquals(0, vm.uiState.value.speed)
        assertFalse(vm.uiState.value.speedWasInvalid)
    }

    @Test
    fun motorOffDoesNotForceCoastingSpeedToZero() = vehicleTest {
        val vm = model(flow {
            emit(VehicleUiState(motorOn = false, speed = 25))
            awaitCancellation()
        })
        runCurrent()
        assertEquals(25, vm.uiState.value.speed)
        assertFalse(vm.uiState.value.motorOn)
    }

    @Test
    fun staleSignalRecoversAndFreshSampleRestartsDeadline() = vehicleTest {
        val source = MutableSharedFlow<VehicleUiState>(extraBufferCapacity = 1)
        val vm = model(source)
        runCurrent()
        source.emit(VehicleUiState(speed = 25))
        runCurrent()
        advanceTimeBy(2_000)
        source.emit(VehicleUiState(speed = 50))
        runCurrent()
        advanceTimeBy(2_999)
        runCurrent()
        assertEquals(SignalStatus.LIVE, vm.uiState.value.signalStatus)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(SignalStatus.STALE, vm.uiState.value.signalStatus)
        source.emit(VehicleUiState(speed = 0, rideState = RideState.READY))
        runCurrent()
        assertEquals(SignalStatus.LIVE, vm.uiState.value.signalStatus)
        assertEquals(0, vm.uiState.value.speed)
        assertEquals(RideState.READY, vm.uiState.value.rideState)
    }

    @Test
    fun noInitialDataBecomesUnavailableAndCanRecover() = vehicleTest {
        val source = MutableSharedFlow<VehicleUiState>(extraBufferCapacity = 1)
        val vm = model(source)
        runCurrent()
        assertEquals(SignalStatus.WAITING, vm.uiState.value.signalStatus)
        advanceTimeBy(VehicleViewModel.STALE_AFTER_MS)
        runCurrent()
        assertEquals(SignalStatus.UNAVAILABLE, vm.uiState.value.signalStatus)
        source.emit(VehicleUiState(speed = 25))
        runCurrent()
        assertEquals(SignalStatus.LIVE, vm.uiState.value.signalStatus)
    }

    @Test
    fun repositoryFailureIsUnavailableAndWatchdogDoesNotOverwriteIt() = vehicleTest {
        val vm = model(flow {
            emit(VehicleUiState(speed = 25))
            throw IllegalStateException("Simulated provider failure")
        })
        runCurrent()
        assertEquals(SignalStatus.UNAVAILABLE, vm.uiState.value.signalStatus)
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(SignalStatus.UNAVAILABLE, vm.uiState.value.signalStatus)
    }

    @Test
    fun completedStreamIsUnavailable() = vehicleTest {
        val vm = model(emptyFlow())
        runCurrent()
        assertEquals(SignalStatus.UNAVAILABLE, vm.uiState.value.signalStatus)
    }

    @Test
    fun clearingViewModelCancelsSimulationWithoutReportingFailure() = vehicleTest {
        val vm = model(SimulatedVehicleRepository())
        runCurrent()
        val lastState = vm.uiState.value
        store.clear()
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(lastState, vm.uiState.value)
    }
}
