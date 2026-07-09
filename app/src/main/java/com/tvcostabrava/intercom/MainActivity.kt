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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
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

            MaterialTheme {
                Surface(color = Color(0xFF121414)) {
                    if (showSettings) {
                        SettingsScreen(
                            initialUrl = SettingsStore.getServerUrl(this@MainActivity),
                            onSave = { url ->
                                SettingsStore.setServerUrl(this@MainActivity, url)
                                service?.updateServerUrl(url)
                                showSettings = false
                            },
                        )
                    } else {
                        IntercomScreen(
                            onTalkPressed = { pressed -> service?.setPttPressed(pressed) },
                            onHandsFreeToggled = { enabled -> service?.setHandsFree(enabled) },
                            onOpenSettings = { showSettings = true },
                        )
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
fun IntercomScreen(
    onTalkPressed: (Boolean) -> Unit,
    onHandsFreeToggled: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var isTalking by remember { mutableStateOf(false) }
    var handsFree by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Ajustes",
            color = Color(0xFF8A8D8D),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onOpenSettings() })
                },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            val talkColor = if (isTalking || handsFree) Color(0xFF27AE60) else Color(0xFF2ECC71)

            Surface(
                shape = CircleShape,
                color = talkColor,
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(handsFree) {
                        detectTapGestures(
                            onPress = {
                                if (!handsFree) {
                                    isTalking = true
                                    onTalkPressed(true)
                                    tryAwaitRelease()
                                    isTalking = false
                                    onTalkPressed(false)
                                }
                            },
                        )
                    },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = if (handsFree) "HABLANDO" else "HABLAR",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (handsFree) Color(0xFF2ECC71) else Color(0xFF2A2D2D),
                modifier = Modifier
                    .padding(top = 40.dp)
                    .size(width = 220.dp, height = 64.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                handsFree = !handsFree
                                onHandsFreeToggled(handsFree)
                                if (!handsFree) onTalkPressed(false)
                            },
                        )
                    },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "MANOS LIBRES",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(initialUrl: String, onSave: (String) -> Unit) {
    var url by remember { mutableStateOf(initialUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Servidor de señalización",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Pega aquí la URL wss:// de tu servidor (Render u otro)",
            color = Color(0xFF8A8D8D),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            singleLine = true,
            placeholder = { Text("wss://tu-servidor.onrender.com") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { onSave(url.trim()) },
            enabled = url.isNotBlank(),
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("GUARDAR")
        }
    }
}
