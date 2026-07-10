package com.tvcostabrava.intercom

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    activeChannels: Set<String>,
    onChannelsChanged: (Set<String>) -> Unit,
) {
    var isTalking by remember { mutableStateOf(false) }
    var handsFree by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val pal = LocalRetroPalette.current

    val talking = isTalking || handsFree

    fun toggleChannel(channelId: String) {
        val allDeptIds = DEPARTMENT_CHANNELS.map { it.id }.toSet()
        val newChannels = when (channelId) {
            TODOS_ID -> if (activeChannels.containsAll(allDeptIds)) emptySet() else allDeptIds
            else -> if (channelId in activeChannels) activeChannels - channelId else activeChannels + channelId
        }
        onChannelsChanged(newChannels)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pal.surfaceContainerHighest)
            .padding(12.dp)
            .clip(RoundedCornerShape(32.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(pal.surfaceContainerHigh),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                ChassisHeader()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SpeakerLcdModule()
                    BrandModelRow()
                    ChannelSwitchGrid(
                        activeChannels = activeChannels,
                        onToggle = ::toggleChannel,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LedPillRow(talking = talking)

                    Spacer(modifier = Modifier.height(12.dp))
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
                        color = pal.outlineVariant,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )

                    Spacer(modifier = Modifier.height(12.dp))
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
                        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                    ) {
                        DecorLine()
                        Text(
                            text = "HANDS FREE MODE",
                            color = pal.onSurfaceVariant.copy(alpha = 0.4f),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        DecorLine()
                    }
                }
            }

            BottomNavBar(
                selected = selectedTab,
                onSelect = { i ->
                    if (i == SETTINGS_TAB_INDEX) onOpenSettings() else selectedTab = i
                },
            )
        }

        ScrewIcon(
            color = pal.onSurface,
            modifier = Modifier.align(Alignment.TopStart).padding(20.dp, 84.dp, 0.dp, 0.dp),
        )
        ScrewIcon(
            color = pal.onSurface,
            modifier = Modifier.align(Alignment.TopEnd).padding(0.dp, 84.dp, 20.dp, 0.dp),
        )
    }
}

private const val SETTINGS_TAB_INDEX = 2

@Composable
private fun ChassisHeader() {
    val pal = LocalRetroPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(pal.surfaceContainerHighest)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AntennaIcon(color = pal.primaryLight, size = 14.dp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "RADIO UNIT-82",
            color = pal.primaryLight,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.weight(1f),
        )
        BatteryIcon(color = pal.primaryLight, size = 16.dp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpeakerLcdModule() {
    val pal = LocalRetroPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(pal.speakerGrille)
            .border(3.dp, pal.surfaceContainerLowest, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        CornerDot(color = pal.onSurface, modifier = Modifier.align(Alignment.TopStart).padding(3.dp))
        CornerDot(color = pal.onSurface, modifier = Modifier.align(Alignment.TopEnd).padding(3.dp))
        CornerDot(color = pal.onSurface, modifier = Modifier.align(Alignment.BottomStart).padding(3.dp))
        CornerDot(color = pal.onSurface, modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(RetroColors.LcdBackground)
                .border(2.dp, pal.surfaceVariant, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Radio Alicante Libre",
                color = pal.primaryLight,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(),
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "SIGNAL",
                    color = pal.primaryLight.copy(alpha = 0.5f),
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) { i ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(9.dp)
                                .background(
                                    if (i < 3) pal.primaryLight.copy(alpha = 1f - i * 0.15f)
                                    else pal.surfaceContainer.copy(alpha = 0.5f),
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
    val pal = LocalRetroPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = "SIGNAL-84",
            color = pal.onSurfaceVariant,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            letterSpacing = (-0.5).sp,
        )
        Text(
            text = "HIGH FIDELITY TRANSCEIVER",
            color = pal.outline,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(pal.outlineVariant),
    )
}

@Composable
private fun ChannelSwitchGrid(activeChannels: Set<String>, onToggle: (String) -> Unit) {
    val allDeptActive = activeChannels.containsAll(DEPARTMENT_CHANNELS.map { it.id })

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DEPARTMENT_CHANNELS.forEach { channel ->
            ChannelSwitch(
                label = channel.label,
                active = channel.id in activeChannels,
                modifier = Modifier.weight(1f),
                onClick = { onToggle(channel.id) },
            )
        }
        ChannelSwitch(
            label = "TODOS",
            active = allDeptActive,
            modifier = Modifier.weight(1f),
            onClick = { onToggle(TODOS_ID) },
        )
    }
}

@Composable
private fun ChannelSwitch(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val pal = LocalRetroPalette.current
    val currentOnClick by androidx.compose.runtime.rememberUpdatedState(onClick)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(
                id = if (active) R.drawable.channel_switch_on else R.drawable.channel_switch_off,
            ),
            contentDescription = label,
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
            modifier = Modifier
                .width(40.dp)
                .height(69.dp)
                .clip(RoundedCornerShape(6.dp))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { currentOnClick() })
                },
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (active) RetroColors.PrimaryContainer else pal.onSurfaceVariant,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun LedPillRow(talking: Boolean) {
    val pal = LocalRetroPalette.current
    val infinite = rememberInfiniteTransition(label = "tx-blink")
    val blink by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "tx-blink-alpha",
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = pal.surfaceContainerLowest.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, pal.outlineVariant.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(RetroColors.TxRed.copy(alpha = if (talking) 1f else blink), CircleShape),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("TX", color = pal.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(RetroColors.RxGreen.copy(alpha = 0.5f), CircleShape),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("RX", color = pal.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
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
            .offset(y = if (talking) 4.dp else 0.dp)
            .size(128.dp)
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
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            MicIcon(color = Color.White.copy(alpha = if (talking) 1f else 0.5f), size = 20.dp)
        }
    }
}

@Composable
private fun HandsFreeButton(active: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .fillMaxWidth()
            .height(46.dp)
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
            HeadsetIcon(color = RetroColors.OnHandsFree, size = 15.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "MANOS LIBRES",
                color = RetroColors.OnHandsFree,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
            )
            if (active) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(RetroColors.RxGreen, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun DecorLine() {
    val pal = LocalRetroPalette.current
    Box(
        modifier = Modifier
            .width(32.dp)
            .height(1.dp)
            .background(pal.surfaceContainerLowest.copy(alpha = 0.4f)),
    )
}

@Composable
private fun BottomNavBar(selected: Int, onSelect: (Int) -> Unit) {
    val pal = LocalRetroPalette.current
    val items = listOf("TRANSCEIVER", "CHANNELS", "SETTINGS", "LOG")

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(pal.surfaceVariant),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(pal.surfaceContainerHigh)
                .padding(horizontal = 4.dp, vertical = 6.dp),
        ) {
            items.forEachIndexed { i, label ->
                val active = i == selected
                val tint = if (active) RetroColors.OnPrimaryContainer else pal.onSurfaceVariant
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .then(if (active) Modifier.background(RetroColors.PrimaryContainer) else Modifier)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .pointerInput(i) {
                            detectTapGestures(onTap = { onSelect(i) })
                        },
                ) {
                    when (label) {
                        "TRANSCEIVER" -> RadioIcon(color = tint, size = 15.dp)
                        "CHANNELS" -> ChannelsIcon(color = tint, size = 15.dp)
                        "SETTINGS" -> GearIcon(color = tint, size = 15.dp)
                        else -> HistoryIcon(color = tint, size = 15.dp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        color = tint.copy(alpha = if (active) 1f else 0.8f),
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.2.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
