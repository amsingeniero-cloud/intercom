package com.tvcostabrava.intercom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla principal con el look "Radio Unit-82" (mockup Stitch: radio industrial 80s).
 * Solo HABLAR y MANOS LIBRES son funcionales; el resto (canales, TX/RX, barra inferior,
 * volumen) es decorativo para mantener la app "muy simple" por dentro.
 */
@Composable
fun RetroWalkieScreen(
    onTalkPressed: (Boolean) -> Unit,
    onHandsFreeToggled: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var isTalking by remember { mutableStateOf(false) }
    var handsFree by remember { mutableStateOf(false) }
    var selectedChannel by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Surface(color = RetroColors.Chassis) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            RetroHeader(onOpenSettings = onOpenSettings)

            Spacer(modifier = Modifier.height(12.dp))
            ChannelSelectorRow(
                selected = selectedChannel,
                onSelect = { selectedChannel = it },
            )

            Spacer(modifier = Modifier.height(12.dp))
            TransceiverPanel(isTalking = isTalking || handsFree)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TalkButton(
                    active = isTalking || handsFree,
                    enabledForPress = !handsFree,
                    onPress = { pressed ->
                        isTalking = pressed
                        onTalkPressed(pressed)
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))

                HandsFreeButton(
                    active = handsFree,
                    onToggle = {
                        handsFree = !handsFree
                        onHandsFreeToggled(handsFree)
                        if (!handsFree) onTalkPressed(false)
                    },
                )
            }

            BottomNavBar(selected = selectedTab, onSelect = { selectedTab = it })

            Spacer(modifier = Modifier.height(8.dp))
            RetroFooter()
        }
    }
}

@Composable
private fun RetroHeader(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "RADIO UNIT-82",
                color = RetroColors.OnSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
            )
            Text(
                text = "SIGNAL-84",
                color = RetroColors.OnSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            AntennaIcon(color = RetroColors.OnSurfaceVariant, size = 20.dp)
            Spacer(modifier = Modifier.width(10.dp))
            BatteryIcon(color = RetroColors.LedGreen, size = 20.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "CFG",
                color = RetroColors.Outline,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onTap = { onOpenSettings() })
                },
            )
        }
    }
}

@Composable
private fun ChannelSelectorRow(selected: Int, onSelect: (Int) -> Unit) {
    Surface(
        color = RetroColors.SurfaceContainer,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RetroColors.OutlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(14.dp)
                        .border(
                            width = 2.dp,
                            color = if (i == selected) RetroColors.PrimaryOrange else RetroColors.Outline,
                            shape = CircleShape,
                        )
                        .pointerInput(i) {
                            detectTapGestures(onTap = { onSelect(i) })
                        },
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "RADIO ALICANTE LIBRE",
                color = RetroColors.OnSurface,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
private fun TransceiverPanel(isTalking: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        color = RetroColors.SurfaceContainerLow,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RetroColors.OutlineVariant),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "SIGNAL-84 · HIGH FIDELITY TRANSCEIVER",
                color = RetroColors.OnSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                LcdReadout(label = "TX", lit = isTalking, color = RetroColors.PrimaryOrange, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                LcdReadout(label = "RX", lit = !isTalking, color = RetroColors.LedGreen, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LcdReadout(label: String, lit: Boolean, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = RetroColors.LcdBackground,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (lit) color else color.copy(alpha = 0.25f), CircleShape),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (lit) color else RetroColors.OnSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun TalkButton(active: Boolean, enabledForPress: Boolean, onPress: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (active) RetroColors.PrimaryOrange.copy(alpha = 0.85f) else RetroColors.PrimaryOrange,
        shadowElevation = if (active) 1.dp else 8.dp,
        modifier = Modifier
            .size(180.dp)
            .pointerInput(enabledForPress) {
                detectTapGestures(
                    onPress = {
                        if (enabledForPress) {
                            onPress(true)
                            tryAwaitRelease()
                            onPress(false)
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
            MicIcon(color = RetroColors.OnPrimary, size = 40.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "HABLAR",
                color = RetroColors.OnPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "PRESS TO TALK",
                color = RetroColors.OnPrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun HandsFreeButton(active: Boolean, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = RetroColors.SurfaceContainerHigh,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) RetroColors.LedGreen else RetroColors.OutlineVariant,
        ),
        modifier = Modifier
            .width(220.dp)
            .height(64.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onToggle() })
            },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeadsetIcon(color = if (active) RetroColors.LedGreen else RetroColors.OnSurfaceVariant, size = 18.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "MANOS LIBRES",
                    color = if (active) RetroColors.LedGreen else RetroColors.OnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "HANDS FREE MODE",
                    color = RetroColors.OnSurfaceVariant,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (active) RetroColors.LedGreen else RetroColors.Outline.copy(alpha = 0.3f), CircleShape),
            )
        }
    }
}

@Composable
private fun BottomNavBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf("TRANSCEIVER", "CHANNELS", "SQUELCH", "LOG")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        color = RetroColors.SurfaceContainer,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RetroColors.OutlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEachIndexed { i, label ->
                val tint = if (i == selected) RetroColors.PrimaryOrange else RetroColors.OnSurfaceVariant
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.pointerInput(i) {
                        detectTapGestures(onTap = { onSelect(i) })
                    },
                ) {
                    when (label) {
                        "TRANSCEIVER" -> RadioIcon(color = tint, size = 18.dp)
                        "CHANNELS" -> ChannelsIcon(color = tint, size = 18.dp)
                        "SQUELCH" -> TuneIcon(color = tint, size = 18.dp)
                        else -> HistoryIcon(color = tint, size = 18.dp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = tint,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun RetroFooter() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row {
            repeat(10) { i ->
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(4.dp)
                        .background(RetroColors.OutlineVariant, CircleShape),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "VOLUME",
                color = RetroColors.OnSurfaceVariant,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .background(RetroColors.SurfaceContainerHigh, RoundedCornerShape(2.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(4.dp)
                        .background(RetroColors.PrimaryOrange, RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}
