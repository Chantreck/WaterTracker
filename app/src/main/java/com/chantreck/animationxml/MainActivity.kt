package com.chantreck.animationxml

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chantreck.animationxml.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var drunk = 0
    private var remain = MAX_AMOUNT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            remain = (remain - STEP).coerceAtLeast(0)
            drunk += STEP

            redraw()
        }
    }

    private fun redraw() {
        binding.drunkLabel.text = "Выпито:\n $drunk мл"
        binding.remainLabel.text = "Осталось:\n $remain мл"

        val percent = drunk.toFloat() / MAX_AMOUNT
        val remainPercent = 1 - percent

        binding.waterLevel.setGuidelinePercent(remainPercent)
        binding.water.alpha = percent

        if (remainPercent < DRUNK_THRESHOLD) {
            binding.drunkLabel.setTextColor(Color.WHITE)
        }

        if (remainPercent < REMAIN_THRESHOLD) {
            binding.remainLabel.setTextColor(Color.WHITE)
        }
    }

    private companion object {
        const val MAX_AMOUNT = 2500
        const val STEP = 250

        const val REMAIN_THRESHOLD = 0.2f
        const val DRUNK_THRESHOLD = 0.4f
    }
}