package com.chantreck.animationxml

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.chantreck.animationxml.theme.AnimationXMLTheme
import com.chantreck.animationxml.theme.WaterColor

@Composable
fun WearApp(state: State, onStateUpdate: (State) -> Unit) {
    AnimationXMLTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            DrawWater(state = state, modifier = Modifier.align(Alignment.BottomCenter))
            DrawContent(state = state, onStateUpdate = onStateUpdate)
        }
    }
}

@Composable
private fun DrawWater(state: State, modifier: Modifier) {
    val alpha by animateFloatAsState(
        targetValue = state.percentage,
        animationSpec = tween(durationMillis = ANIMATION_DURATION)
    )

    Box(
        modifier
            .animateContentSize(animationSpec = tween(durationMillis = ANIMATION_DURATION))
            .fillMaxWidth()
            .fillMaxHeight(state.percentage)
            .background(WaterColor.copy(alpha = alpha))
    )
}

@Composable
private fun DrawContent(state: State, onStateUpdate: (State) -> Unit) {
    Button(
        onClick = {
            onStateUpdate(state)
        },
    ) {
        Text(
            text = "+${MainActivity.STEP} мл",
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
private fun DefaultPreview() {
    WearApp(state = State(), onStateUpdate = {})
}

private const val ANIMATION_DURATION = 750