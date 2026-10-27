# V Dash - Android Vehicle HMI

A landscape Android dashboard built with Kotlin and Jetpack Compose. It displays vehicle speed, motor status, kill-switch status, and the current ride scenario using a simulated data source.

No vehicle hardware, CAN integration, backend, or account setup is required to run the demo.

## Features

- Speedometer with a numeric km/h reading and animated gauge.
- Motor ON/OFF and kill-switch Active/Inactive indicators.
- Ride-state card and five-stage sequence indicator.
- Automatic state updates through Coroutines, Flow, and StateFlow.
- Lifecycle-aware state collection in Compose.
- Waiting, stale-data, and unavailable-data screens.
- Negative-speed handling with a visible warning.
- Optional bottom metrics bar with fixed demo values: ODO 1,234 km, battery 78%, estimated range 92 km, and temperature 32 °C.

## Tech stack

- Kotlin and Jetpack Compose / Material 3
- AndroidX ViewModel and lifecycle-aware Compose collection
- Kotlin Coroutines and Flow / StateFlow
- Hilt dependency injection with KSP
- JUnit 4 and kotlinx-coroutines-test

## Build and run

### Prerequisites

Use an Android Studio installation compatible with the versions declared in the project:

| Setting | Current configuration |
| --- | --- |
| Android Gradle Plugin | 9.3.3 |
| Gradle wrapper | 9.8.0 |
| Kotlin | 2.4.21 |
| Gradle daemon JVM toolchain | JDK 25 |
| Java source/target compatibility | Java 11 |
| Compile SDK | API 37 |
| Target SDK | API 35 |
| Minimum Android version | API 24 / Android 7.0 |

The daemon JVM requirement and Java source compatibility are different settings. The daemon toolchain is configured in `gradle/gradle-daemon-jvm.properties`.

Install Android SDK Platform 37 through SDK Manager. A compatible emulator system image or an Android device running API 24 or later is needed to launch the app. Internet access may be required for the initial Gradle, toolchain, and dependency downloads.

### Android Studio

1. Clone or download this repository.
2. Open the directory containing `settings.gradle.kts`.
3. Let Android Studio configure the local SDK location and complete Gradle sync.
4. Create/start an Android emulator, or connect a device with USB debugging enabled.
5. Select the `app` run configuration and press **Run**.

The main activity requests landscape orientation and hides system bars for the instrument-cluster display.

### Command line

Run these commands from the project root:

```bash
./gradlew :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To install it on a connected emulator or device:

```bash
./gradlew :app:installDebug
```

Launch **V Dash** from the device launcher. On Windows, use `gradlew.bat` instead of `./gradlew`. If the wrapper is not executable on macOS/Linux, run `chmod +x gradlew` once.

## Architecture

```text
SimulatedVehicleRepository
        |
        | Flow<VehicleUiState> through VehicleRepository
        v
VehicleViewModel
        |
        | StateFlow<VehicleUiState>
        v
Dashboard (collectAsStateWithLifecycle)
        |
        v
DashboardContent and reusable cards
```

| Component | Responsibility |
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


## Submission checklist

- [ ] Correct the one-second sequence test timing and rerun all unit tests.
- [ ] Build the latest source with `./gradlew :app:assembleDebug`.
- [ ] Complete the landscape emulator acceptance check.
- [ ] Include source code, Gradle wrapper, build configuration, tests, and this README.
- [ ] Exclude generated build directories, local SDK paths, and machine-specific files.
- [ ] Provide the GitHub repository URL to the reviewer.
