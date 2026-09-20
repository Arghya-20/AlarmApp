package com.arghya.alarmapp.alarm

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.arghya.alarmapp.ui.theme.AlarmAppTheme
import com.arghya.alarmapp.ui.theme.AppThemeMode
import com.arghya.alarmapp.ui.theme.NightPalette

class AlarmRingActivity : ComponentActivity() {

    private var shakeDetector: ShakeDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        val label = intent.getStringExtra("label") ?: "Alarm"
        val shakeEnabled = intent.getBooleanExtra("shake_to_stop", true)

        setContent {
            AlarmAppTheme(
                themeMode = AppThemeMode.DARK,
                systemDark = true,
                nightPalette = NightPalette.MIDNIGHT_BLACK
            ) {
                RingScreen(
                    label = label,
                    shakeEnabled = shakeEnabled,
                    onDismiss = { stopAlarm() }
                )
            }
        }

        if (shakeEnabled) {
            shakeDetector = ShakeDetector(this) { stopAlarm() }
            shakeDetector?.start()
        }
    }

    private fun stopAlarm() {
        shakeDetector?.stop()
        AlarmRingService.stop(this)
        finish()
    }

    override fun onDestroy() {
        shakeDetector?.stop()
        super.onDestroy()
    }
}

@Composable
private fun RingScreen(label: String, shakeEnabled: Boolean, onDismiss: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val scale by infinite.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "scale"
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Alarm,
                contentDescription = null,
                modifier = Modifier.size((96 * scale).dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(24.dp))
            Text(label, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            if (shakeEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Shake your phone to stop", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                }
                Spacer(Modifier.height(32.dp))
            } else {
                Spacer(Modifier.height(32.dp))
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Stop", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
