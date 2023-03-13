package com.chantreck.animationxml

import android.animation.ValueAnimator
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.chantreck.animationxml.databinding.ActivityMainBinding
import com.google.android.gms.wearable.*

class MainActivity : AppCompatActivity(R.layout.activity_main), DataClient.OnDataChangedListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val putDataMapRequest by lazy { PutDataMapRequest.create(MESSAGE_PATH) }
    private var state = State()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.button.apply {
            text = "+$STEP мл"
            setOnClickListener {
                updateState(state)
            }
        }
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
                putInt(KEY_DRUNK, state.drunk)
                putInt(KEY_REMAIN, state.remain)
                putFloat(KEY_PERCENTAGE, state.percentage)
            }
            setUrgent()
            asPutDataRequest()
        }
        dataClient.putDataItem(request)
    }

    private fun DataEventBuffer.getStateFromWear() = forEach { event ->
        if (event.type == DataEvent.TYPE_CHANGED) {
            val item = event.dataItem
            val path = item.uri.path ?: return@forEach

            if (path == MESSAGE_PATH) {
                val newState = DataMapItem.fromDataItem(item).dataMap.extractState()
                redraw(newState)
                state = newState
            }
        }
    }

    private fun DataMap.extractState(): State = State(
        drunk = getInt(KEY_DRUNK),
        remain = getInt(KEY_REMAIN),
        percentage = getFloat(KEY_PERCENTAGE)
    )

    private fun redraw(newState: State) {
        binding.drunkLabel.text = "Выпито:\n ${newState.drunk} мл"
        binding.remainLabel.text = "Осталось:\n ${newState.remain} мл"

        animateWaterLevelRising(oldLevel = state.percentage, newLevel = newState.percentage)

        if (newState.percentage == DRUNK_THRESHOLD) {
            changeTextColor(binding.drunkLabel)
        }

        if (newState.percentage == REMAIN_THRESHOLD) {
            changeTextColor(binding.remainLabel)
        }
    }

    private fun animateWaterLevelRising(oldLevel: Float, newLevel: Float) {
        ValueAnimator.ofFloat(oldLevel, newLevel).apply {
            duration = ANIMATION_DURATION
            addUpdateListener {
                val percent = it.animatedValue as Float
                val remainPercent = 1 - percent

                binding.water.alpha = percent
                binding.waterLevel.setGuidelinePercent(remainPercent)
            }
            start()
        }
    }

    private fun changeTextColor(textView: TextView, newColor: Int = Color.WHITE) {
        val oldColor: Int = textView.currentTextColor
        if (newColor == oldColor) return

        ValueAnimator.ofArgb(oldColor, newColor).apply {
            duration = ANIMATION_DURATION
            addUpdateListener {
                val value = it.animatedValue as Int
                textView.setTextColor(value)
            }
            start()
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.getStateFromWear()
    }

    companion object {
        const val MAX_AMOUNT = 2500
        private const val STEP = 250

        private const val REMAIN_THRESHOLD = 0.8f
        private const val DRUNK_THRESHOLD = 0.6f
        private const val ANIMATION_DURATION: Long = 750

        private const val MESSAGE_PATH = "/state"
        private const val KEY_DRUNK = "DRUNK"
        private const val KEY_REMAIN = "REMAIN"
        private const val KEY_PERCENTAGE = "PERCENTAGE"
    }
}