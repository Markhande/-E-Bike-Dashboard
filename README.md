# E-Bike Dashboard

## Screenshots

<table>
  <tr>
    <td align="center"><img src="docs/screenshots/parked.png" alt="Parked state" width="100%"></td>
    <td align="center"><img src="docs/screenshots/ready.png" alt="Ready state" width="100%"></td>
    <td align="center"><img src="docs/screenshots/riding.png" alt="Riding state" width="100%"></td>
  </tr>
  <tr>
    <td align="center"><b>Parked</b><br>0 km/h, motor OFF</td>
    <td align="center"><b>Ready</b><br>0 km/h, motor ON</td>
    <td align="center"><b>Riding</b><br>25 km/h, motor ON</td>
  </tr>
</table>

| File | Responsibility |
| --- | --- |
| `model/VehicleUiState.kt` | Vehicle readings, ride scenario, signal availability, and invalid-speed flag |
| `repository/VehicleRepository.kt` | Data-provider interface |
| `repository/SimulatedVehicleRepository.kt` | Repeating simulated vehicle sequence |
| `viewmodel/VehicleViewModel.kt` | State collection, speed validation, timeout monitoring, and error handling |
| `di/VehicleModule.kt` | Hilt repository binding |
| `view/HomeScreen.kt` | Lifecycle-aware collection and dashboard composition |
| `view/*Card.kt`, `view/SpeedoMeter.kt` | Individual dashboard components |
| `view/MetricsBar.kt` | Bottom metrics presentation |

The project uses one Gradle app module with separate packages for UI, state management, and the data provider. `DashboardContent` accepts a state value directly, allowing Compose previews without constructing a Hilt ViewModel.

For a future real vehicle source, implement `VehicleRepository` and change the binding in `VehicleModule`. Connection management and protocol decoding belong in the data layer. A separate raw vehicle-data model would be a useful next step for production integration; the current demo shares `VehicleUiState` between the provider and ViewModel.

## Simulation

The repository emits the first scenario immediately, then advances approximately once per second. After the last scenario, the sequence repeats.

| Scenario | Speed (km/h) | Motor | Kill switch |
| --- | ---: | --- | --- |
| Parked | 0 | OFF | Inactive |
| Ready | 0 | ON | Inactive |
| Riding | 25 | ON | Inactive |
| Accelerating | 50 | ON | Inactive |
| Motor disabled | 0 | OFF | Active |

These are demo samples, not vehicle safety rules. Motor OFF does not force speed to zero; a moving vehicle may coast.

The bottom metrics are fixed presentation values and are not emitted by the simulation.

## Edge cases and failure handling

| Condition | Behaviour |
| --- | --- |
| Negative speed | ViewModel clamps it to 0 and sets a warning flag; UI displays an invalid-speed warning |
| No initial sample | UI starts in WAITING and becomes UNAVAILABLE after 3 seconds |
| No update after a live sample | UI changes to STALE after 3 seconds |
| Fresh sample after a timeout | State returns to LIVE and the timeout restarts |
| Provider exception or normal stream completion | UI becomes UNAVAILABLE |
| ViewModel cleared | Collection and watchdog jobs are cancelled without reporting cancellation as a provider failure |

When data is not live, the UI hides the normal dashboard and metrics and displays unavailable readings as dashes. It does not present the retained sample as current telemetry.

A terminated or failed stream is not automatically retried. Recreating the ViewModel, for example by relaunching the activity, starts a new subscription.

## Unit tests

Run the local JVM tests without an emulator:

```bash
./gradlew :app:testDebugUnitTest
```

Run only the vehicle state tests:

```bash
./gradlew :app:testDebugUnitTest --tests "com.myworkshopy.vdash.VehicleViewModelTest"
```

The HTML report is generated at:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

`VehicleViewModelTest` contains eight tests covering:

- All five scenario values, emission timing, and looping.
- Negative-speed correction and warning reset.
- Nonzero speed while the motor is OFF.
- Stale-data detection, deadline reset, and recovery.
- Missing initial data and recovery.
- Provider failure and watchdog cancellation.
- Normal stream completion.
- ViewModel lifecycle cancellation.

Tests use virtual coroutine time rather than real waits. The project also retains the Android template's arithmetic unit test; it is not part of the meaningful vehicle coverage.

### Current test timing correction required

The simulation now uses `delay(1_000L)`, but the sequence test still has the name `simulationUpdatesAllFiveStatesEveryTwoSecondsAndLoops` and advances by `1_999` milliseconds before checking for an early update.

Before submission, rename that test for the one-second interval and change `advanceTimeBy(1_999)` to `advanceTimeBy(999)`. Keep the following one-millisecond advance, then rerun the suite. Earlier passing reports used the previous two-second simulation and do not establish that the current timing test passes.

## Manual acceptance check

On a landscape Android emulator:

1. Launch the app and watch at least one complete five-scenario cycle.
2. Check that speed, motor, kill switch, ride-state text, and sequence highlight agree.
3. Confirm the three zero-speed scenarios display 0 km/h.
4. Check that labels, icons, gauge, and bottom metrics fit without clipping.
5. Background and reopen the app to check state collection and display behaviour.

The unit tests exercise the timeout and error paths using controlled providers. The default simulator does not deliberately inject failures. There is no dedicated automated dashboard UI test suite, and final emulator visual acceptance remains to be completed.

## Assumptions and limitations

- This is an HMI demonstration, not production vehicle-control software.
- The speedometer scale is 0-120 km/h; the current gauge implementation clamps displayed speed to that range.
- Ride scenarios come from the simulated provider rather than being inferred from speed or motor status.
- Readings use integer speed values and a fixed three-second freshness timeout.
- The dashboard is designed for landscape and scales from a reference layout; small displays and enlarged system fonts need visual checks.
- Odometer, battery, estimated range, and temperature are static dummy values.
- No hardware transport, persistence, backend integration, or automatic retry after a terminated stream is implemented.

## Submission checklist

- [ ] Correct the one-second sequence test timing and rerun all unit tests.
- [ ] Build the latest source with `./gradlew :app:assembleDebug`.
- [ ] Complete the landscape emulator acceptance check.
- [ ] Include source code, Gradle wrapper, build configuration, tests, and this README.
- [ ] Exclude generated build directories, local SDK paths, and machine-specific files.
- [ ] Provide the GitHub repository URL to the reviewer.
