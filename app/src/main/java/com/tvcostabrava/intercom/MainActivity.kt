package com.tvcostabrava.intercom

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var service: IntercomService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = (binder as IntercomService.LocalBinder).getService()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            bound = false
        }
    }

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) startAndBindService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startAndBindService()
        } else {
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            var showSettings by remember {
                mutableStateOf(SettingsStore.getServerUrl(this@MainActivity).isBlank())
            }
            var isDark by remember {
                mutableStateOf(SettingsStore.isDarkTheme(this@MainActivity))
            }
            val palette = if (isDark) DarkRetroPalette else LightRetroPalette

            CompositionLocalProvider(LocalRetroPalette provides palette) {
                MaterialTheme {
                    Surface(color = palette.background) {
                        if (showSettings) {
                            SettingsScreen(
                                initialUrl = SettingsStore.getServerUrl(this@MainActivity),
                                isDark = isDark,
                                onThemeChange = { dark ->
                                    isDark = dark
                                    SettingsStore.setDarkTheme(this@MainActivity, dark)
                                },
                                onSave = { url ->
                                    SettingsStore.setServerUrl(this@MainActivity, url)
                                    service?.updateServerUrl(url)
                                    showSettings = false
                                },
                            )
                        } else {
                            RetroWalkieScreen(
                                onTalkPressed = { pressed -> service?.setPttPressed(pressed) },
                                onHandsFreeToggled = { enabled -> service?.setHandsFree(enabled) },
                                onOpenSettings = { showSettings = true },
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startAndBindService() {
        val intent = Intent(this, IntercomService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        if (bound) {
            unbindService(connection)
            bound = false
        }
        super.onDestroy()
    }
}

@Composable
fun SettingsScreen(
    initialUrl: String,
    isDark: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onSave: (String) -> Unit,
) {
    var url by remember { mutableStateOf(initialUrl) }
    val pal = LocalRetroPalette.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "ESTILO VISUAL",
            color = pal.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )
        Row(
            modifier = Modifier.padding(top = 10.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ThemeOptionButton(label = "OSCURO", selected = isDark, onClick = { onThemeChange(true) })
            ThemeOptionButton(label = "CLARO", selected = !isDark, onClick = { onThemeChange(false) })
        }

        Text(
            text = "SERVIDOR DE SEÑALIZACIÓN",
            color = pal.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
        )
        Text(
            text = "Pega aquí la URL wss:// de tu servidor (Render u otro)",
            color = pal.onSurfaceVariant,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            placeholder = {
                Text(
                    "wss://tu-servidor.onrender.com",
                    color = pal.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                autoCorrect = false,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = pal.onSurface,
                unfocusedTextColor = pal.onSurface,
                cursorColor = RetroColors.PrimaryContainer,
                focusedBorderColor = RetroColors.PrimaryContainer,
                unfocusedBorderColor = pal.outline,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { onSave(url.trim()) },
            enabled = url.isNotBlank(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = RetroColors.PrimaryContainer,
                contentColor = RetroColors.OnPrimaryContainer,
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("GUARDAR", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun ThemeOptionButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val pal = LocalRetroPalette.current
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) RetroColors.PrimaryContainer else pal.surfaceContainerHigh,
        modifier = Modifier
            .height(44.dp)
            .width(110.dp)
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                color = if (selected) RetroColors.OnPrimaryContainer else pal.onSurfaceVariant,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}
