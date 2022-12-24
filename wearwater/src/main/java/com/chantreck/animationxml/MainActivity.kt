package com.chantreck.animationxml

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.chantreck.animationxml.theme.AnimationXMLTheme
import com.chantreck.animationxml.theme.WaterColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    var state by remember { mutableStateOf(State()) }

    AnimationXMLTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            DrawWater(state = state, modifier = Modifier.align(Alignment.BottomCenter))
            DrawContent(state = state) {
                state = it
            }
        }
    }
}

@Composable
private fun DrawWater(state: State, modifier: Modifier) {
    val alpha by animateFloatAsState(targetValue = state.percentage)
    Box(
        modifier
            .fillMaxWidth()
            .animateContentSize()
            .fillMaxHeight(state.percentage)
            .background(WaterColor.copy(alpha = alpha))
    )
}

@Composable
private fun DrawContent(state: State, onStateChange: (State) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Осталось:\n ${state.remain} мл",
            color = Color(0xFF0277BD),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.weight(0.25f))
        Text(
            text = "Выпито:\n ${state.drunk} мл",
            color = Color(0xFF0277BD),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                val drunk = state.drunk + STEP
                val remain = (state.remain - STEP).coerceAtLeast(0)
                val percentage = (drunk.toFloat() / MAX_AMOUNT).coerceAtMost(1f)
                onStateChange(State(drunk, remain, percentage))
            },
        ) {
            Text(text = "+$STEP мл", modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}

private const val STEP = 250
private const val MAX_AMOUNT = 2500

private data class State(
    var drunk: Int = 0,
    var remain: Int = MAX_AMOUNT,
    var percentage: Float = 0f,
)

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}