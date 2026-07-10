package com.tvcostabrava.intercom

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Calco del mockup Stitch "RADIO UNIT-82 | SIGNAL-84" (Walkie-Talkie Radio Alicante Libre).
 * Solo HABLAR y MANOS LIBRES son funcionales; LCD, LEDs TX/RX, corner-screws y barra
 * inferior son decorativos, igual que en el HTML original.
 */
@Composable
fun RetroWalkieScreen(
    onTalkPressed: (Boolean) -> Unit,
    onHandsFreeToggled: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var isTalking by remember { mutableStateOf(false) }
    var handsFree by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    val talking = isTalking || handsFree

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroColors.SurfaceContainerHighest)
            .padding(12.dp)
            .clip(RoundedCornerShape(32.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RetroColors.SurfaceContainerHigh),
        ) {
            ChassisHeader(onOpenSettings = onOpenSettings)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                SpeakerLcdModule()
                BrandModelRow()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                LedPillRow(talking = talking)

                Spacer(modifier = Modifier.height(28.dp))
                TalkButton(
                    talking = talking,
                    enabledForPress = !handsFree,
                    onPress = { pressed ->
                        isTalking = pressed
                        onTalkPressed(pressed)
                    },
                )
                Text(
                    text = "PRESS TO TALK",
                    color = RetroColors.OutlineVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 10.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    DecorLine()
                    Text(
                        text = "HANDS FREE MODE",
                        color = RetroColors.OnSurfaceVariant.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    DecorLine()
                }
            }

            BottomNavBar(selected = selectedTab, onSelect = { selectedTab = it })
        }

        ScrewIcon(
            color = RetroColors.OnSurface,
            modifier = Modifier.align(Alignment.TopStart).padding(20.dp, 84.dp, 0.dp, 0.dp),
        )
        ScrewIcon(
            color = RetroColors.OnSurface,
            modifier = Modifier.align(Alignment.TopEnd).padding(0.dp, 84.dp, 20.dp, 0.dp),
        )
    }
}

@Composable
private fun ChassisHeader(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(RetroColors.SurfaceContainerHighest)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AntennaIcon(color = RetroColors.PrimaryLight, size = 18.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "RADIO UNIT-82",
                color = RetroColors.PrimaryLight,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                maxLines = 1,
                softWrap = false,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            BatteryIcon(color = RetroColors.PrimaryLight, size = 20.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "CFG",
                color = RetroColors.Outline,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onTap = { onOpenSettings() })
                },
            )
        }
    }
}

@Composable
private fun SpeakerLcdModule() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(RetroColors.SpeakerGrille)
            .border(4.dp, RetroColors.SurfaceContainerLowest, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        CornerDot(color = RetroColors.OnSurface, modifier = Modifier.align(Alignment.TopStart).padding(4.dp))
        CornerDot(color = RetroColors.OnSurface, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
        CornerDot(color = RetroColors.OnSurface, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp))
        CornerDot(color = RetroColors.OnSurface, modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(64.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(RetroColors.LcdBackground)
                .border(2.dp, RetroColors.SurfaceVariant, RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Radio Alicante Libre",
                color = RetroColors.PrimaryLight,
                fontSize = 19.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "SIGNAL",
                    color = RetroColors.PrimaryLight.copy(alpha = 0.5f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) { i ->
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(12.dp)
                                .background(
                                    if (i < 3) RetroColors.PrimaryLight.copy(alpha = 1f - i * 0.15f)
                                    else RetroColors.SurfaceContainer.copy(alpha = 0.5f),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandModelRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = "SIGNAL-84",
            color = RetroColors.OnSurfaceVariant,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            letterSpacing = (-0.5).sp,
        )
        Text(
            text = "HIGH FIDELITY TRANSCEIVER",
            color = RetroColors.Outline,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp,
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(RetroColors.OutlineVariant),
    )
}

@Composable
private fun LedPillRow(talking: Boolean) {
    val infinite = rememberInfiniteTransition(label = "tx-blink")
    val blink by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "tx-blink-alpha",
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = RetroColors.SurfaceContainerLowest.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, RetroColors.OutlineVariant.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(RetroColors.TxRed.copy(alpha = if (talking) 1f else blink), CircleShape),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("TX", color = RetroColors.OnSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(RetroColors.RxGreen.copy(alpha = 0.5f), CircleShape),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("RX", color = RetroColors.OnSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun TalkButton(talking: Boolean, enabledForPress: Boolean, onPress: (Boolean) -> Unit) {
    val gradient = if (talking) {
        Brush.radialGradient(listOf(RetroColors.TalkActiveStart, RetroColors.TalkActiveEnd))
    } else {
        Brush.radialGradient(listOf(RetroColors.TalkIdleStart, RetroColors.TalkIdleEnd))
    }

    Box(
        modifier = Modifier
            .offset(y = if (talking) 6.dp else 0.dp)
            .size(190.dp)
            .clip(CircleShape)
            .background(gradient)
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
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "HABLAR",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            MicIcon(color = Color.White.copy(alpha = if (talking) 1f else 0.5f), size = 32.dp)
        }
    }
}

@Composable
private fun HandsFreeButton(active: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.linearGradient(listOf(RetroColors.HandsFreeStart, RetroColors.HandsFreeEnd)))
            .then(
                if (active) {
                    Modifier.border(2.dp, RetroColors.RxGreen, RoundedCornerShape(6.dp))
                } else {
                    Modifier
                },
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onToggle() })
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeadsetIcon(color = RetroColors.OnHandsFree, size = 20.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "MANOS LIBRES",
                color = RetroColors.OnHandsFree,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
            if (active) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(RetroColors.RxGreen, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun DecorLine() {
    Box(
        modifier = Modifier
            .width(32.dp)
            .height(1.dp)
            .background(RetroColors.SurfaceContainerLowest.copy(alpha = 0.4f)),
    )
}

@Composable
private fun BottomNavBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf("TRANSCEIVER", "CHANNELS", "SQUELCH", "LOG")

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(RetroColors.SurfaceVariant),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RetroColors.SurfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEachIndexed { i, label ->
                val active = i == selected
                val tint = if (active) RetroColors.OnPrimaryContainer else RetroColors.OnSurfaceVariant
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .then(if (active) Modifier.background(RetroColors.PrimaryContainer) else Modifier)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .pointerInput(i) {
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
                        color = tint.copy(alpha = if (active) 1f else 0.8f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }
    }
}
