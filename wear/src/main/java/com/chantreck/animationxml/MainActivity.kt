package com.chantreck.animationxml

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.chantreck.animationxml.theme.AnimationXMLTheme
import com.chantreck.animationxml.theme.WaterColor
import com.google.android.gms.wearable.*

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val putDataMapRequest by lazy { PutDataMapRequest.create("/state") }

    private var _state = MutableLiveData<State>()
    private val state: LiveData<State> get() = _state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearApp()
        }
    }

    @Composable
    private fun WearApp() {
        val observedState by state.observeAsState(initial = State())

        AnimationXMLTheme {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                DrawWater(state = observedState, modifier = Modifier.align(Alignment.BottomCenter))
                DrawContent(state = observedState)
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
    private fun DrawContent(state: State) {
        Button(
            onClick = {
                updateState(state)
            },
        ) {
            Text(text = "+$STEP мл", modifier = Modifier.padding(horizontal = 8.dp))
        }
    }

    private fun updateState(oldState: State) {
        val drunk = oldState.drunk + STEP
        val remain = (oldState.remain - STEP).coerceAtLeast(0)
        val percentage = (drunk.toFloat() / MAX_AMOUNT).coerceAtMost(1f)

        val newState = State(drunk, remain, percentage)
        sendStateToPhone(newState)
    }

    private fun sendStateToPhone(state: State) {
        val request = putDataMapRequest.run {
            dataMap.apply {
                putInt("DRUNK", state.drunk)
                putInt("REMAIN", state.remain)
                putFloat("PERCENTAGE", state.percentage)
            }
            setUrgent()
            asPutDataRequest()
        }
        dataClient.putDataItem(request)
    }

    private companion object {
        const val STEP = 250
        const val MAX_AMOUNT = 2500
    }

    data class State(
        var drunk: Int = 0,
        var remain: Int = MAX_AMOUNT,
        var percentage: Float = 0f,
    )

    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
    @Composable
    private fun DefaultPreview() {
        WearApp()
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                event.dataItem.also { item ->
                    if (item.uri.path?.compareTo("/state") == 0) {
                        DataMapItem.fromDataItem(item).dataMap.extractState()
                    }
                }
            }
        }
    }

    private fun DataMap.extractState() {
        val drunk = getInt("DRUNK")
        val remain = getInt("REMAIN")
        val percentage = getFloat("PERCENTAGE")
        _state.value = State(drunk, remain, percentage)
    }
}

