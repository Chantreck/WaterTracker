package com.chantreck.animationxml

data class State(
    var drunk: Int = 0,
    var remain: Int = MainActivity.MAX_AMOUNT,
    var percentage: Float = 0f,
)