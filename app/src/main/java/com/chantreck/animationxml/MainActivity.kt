package com.chantreck.animationxml

import android.animation.ValueAnimator
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chantreck.animationxml.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*

class MainActivity : AppCompatActivity(R.layout.activity_main), DataClient.OnDataChangedListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val putDataMapRequest by lazy { PutDataMapRequest.create("/state") }

    private var state = State()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            updateState(state)
        }
    }

    private fun redraw(oldState: State, newState: State) {
        ValueAnimator.ofFloat(oldState.percentage, newState.percentage).apply {
            duration = 1000
            addUpdateListener {
                val percent = it.animatedValue as Float
                val remainPercent = 1 - percent

                binding.water.alpha = percent
                binding.waterLevel.setGuidelinePercent(remainPercent)

                if (remainPercent < DRUNK_THRESHOLD) {
                    binding.drunkLabel.setTextColor(Color.WHITE)
                }

                if (remainPercent < REMAIN_THRESHOLD) {
                    binding.remainLabel.setTextColor(Color.WHITE)
                }
            }
            start()
        }

        binding.drunkLabel.text = "Выпито:\n ${newState.drunk} мл"
        binding.remainLabel.text = "Осталось:\n ${newState.remain} мл"
    }

    private fun updateState(state: State) {
        val drunk = state.drunk + STEP
        val remain = (state.remain - STEP).coerceAtLeast(0)
        val percentage = (drunk.toFloat() / MAX_AMOUNT).coerceAtMost(1f)
        sendStateToWear(State(drunk, remain, percentage))
    }

    private fun sendStateToWear(state: State) {
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

    data class State(
        var drunk: Int = 0,
        var remain: Int = MAX_AMOUNT,
        var percentage: Float = 0f,
    )

    private companion object {
        const val MAX_AMOUNT = 2500
        const val STEP = 250

        const val REMAIN_THRESHOLD = 0.2f
        const val DRUNK_THRESHOLD = 0.4f
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
        val newState = State(drunk, remain, percentage)
        redraw(oldState = state, newState = newState)
        state = newState
    }
}