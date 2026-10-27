package com.myworkshopy.vdash.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myworkshopy.vdash.R
import com.myworkshopy.vdash.model.RideState
import com.myworkshopy.vdash.model.SignalStatus
import com.myworkshopy.vdash.model.VehicleUiState
import com.myworkshopy.vdash.viewmodel.VehicleViewModel

@Composable
fun HomeScreenBackground(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.misty_futuristic),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun Dashboard(
    viewModel: VehicleViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    DashboardContent(viewModel.uiState.collectAsStateWithLifecycle().value, modifier)
}

// No ViewModel dependency here: previews and all cards share one state snapshot.
@Composable
fun DashboardContent(uiState: VehicleUiState, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        HomeScreenBackground()
        val density = LocalDensity.current
        val scale = minOf(maxWidth / 1120.dp, maxHeight / 432.dp).coerceAtLeast(0.01f)
        CompositionLocalProvider(
            LocalDensity provides Density(density.density * scale, density.fontScale)
        ) {
            Box(Modifier.requiredSize(1120.dp, 432.dp), contentAlignment = Alignment.Center) {
                if (uiState.signalStatus == SignalStatus.LIVE) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MotorStatusCard(uiState.motorOn, Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.weight(0.5f))
                            KillSwitchCard(uiState.killSwitchActive, Modifier.fillMaxWidth())
                        }
                        Speedometer(uiState.speed, Modifier.width(500.dp))
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RideStatusCard(
                                uiState.rideState.label,
                                uiState.motorOn,
                                uiState.killSwitchActive,
                                Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.weight(0.5f))
                            RideSequenceCard(uiState.rideState.ordinal, Modifier.fillMaxWidth())
                        }
                    }
                    if (uiState.speedWasInvalid) {
                        Text(
                            "Invalid speed received • showing 0 km/h",
                            color = Color(0xFFFFCE73),
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.TopCenter)
                                .background(Color(0xEE071827), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    // Hide all live-looking values, including motor/kill-switch status.
                    SignalUnavailable(uiState.signalStatus)
                }
            }
        }
    }
}

@Composable
private fun SignalUnavailable(status: SignalStatus) {
    Column(
        Modifier.background(Color(0xEE071827), RoundedCornerShape(20.dp)).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            when (status) {
                SignalStatus.WAITING -> "WAITING FOR VEHICLE DATA"
                SignalStatus.STALE -> "SIGNAL LOST"
                else -> "VEHICLE DATA UNAVAILABLE"
            },
            color = Color(0xFFFFCE73), fontSize = 24.sp
        )
        Text("— km/h", color = Color.White, fontSize = 48.sp)
        Text("Motor: —   |   Kill switch: —   |   Ride state: —",
            color = Color(0xFFB7CADB), fontSize = 18.sp)
        Text(
            if (status == SignalStatus.STALE) "No update for 3 seconds. Waiting for fresh data."
            else "Live readings will appear when vehicle data is available.",
            color = Color(0xFFB7CADB), fontSize = 16.sp, textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Phone landscape", widthDp = 800, heightDp = 360)
@Preview(name = "Vehicle display", widthDp = 1120, heightDp = 600)
@Composable
private fun DashboardPreview() {
    DashboardContent(VehicleUiState(motorOn = true, speed = 50, rideState = RideState.ACCELERATING))
}

@Preview(name = "Signal lost", widthDp = 800, heightDp = 360)
@Composable
private fun SignalLostPreview() {
    DashboardContent(VehicleUiState(signalStatus = SignalStatus.STALE))
}
