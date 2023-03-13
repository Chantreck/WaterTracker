package com.chantreck.animationxml

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.wearable.*

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val putDataMapRequest by lazy { PutDataMapRequest.create(MESSAGE_PATH) }

    private var _state = MutableLiveData<State>()
    private val state: LiveData<State> get() = _state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val observedState by state.observeAsState(initial = State())
            WearApp(state = observedState) {
                updateState(it)
            }
        }
    }

    private fun updateState(state: State) {
        val drunk = state.drunk + STEP
        val remain = (state.remain - STEP).coerceAtLeast(0)
        val percentage = (drunk.toFloat() / MAX_AMOUNT).coerceAtMost(1f)

        sendStateToPhone(State(drunk, remain, percentage))
    }

    private fun sendStateToPhone(state: State) {
        val request = putDataMapRequest.run {
            dataMap.apply {
                putInt(KEY_DRUNK, state.drunk)
                putInt(KEY_REMAIN, state.remain)
                putFloat(KEY_PERCENTAGE, state.percentage)
            }
            setUrgent()
            asPutDataRequest()
        }
        dataClient.putDataItem(request)
    }

    private fun DataEventBuffer.getStateFromPhone() = forEach { event ->
        if (event.type == DataEvent.TYPE_CHANGED) {
            val item = event.dataItem
            val path = item.uri.path ?: return@forEach

            if (path == MESSAGE_PATH) {
                val newState = DataMapItem.fromDataItem(item).dataMap.extractState()
                _state.value = newState
            }
        }
    }

    private fun DataMap.extractState(): State = State(
        drunk = getInt(KEY_DRUNK),
        remain = getInt(KEY_REMAIN),
        percentage = getFloat(KEY_PERCENTAGE)
    )

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.getStateFromPhone()
    }

    companion object {
        const val MAX_AMOUNT = 2500
        const val STEP = 250

        private const val MESSAGE_PATH = "/state"
        private const val KEY_DRUNK = "DRUNK"
        private const val KEY_REMAIN = "REMAIN"
        private const val KEY_PERCENTAGE = "PERCENTAGE"
    }
}

