package com.solinone.features.calendar

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Direct port of upstream gesture detection from Persian Calendar (DetectSwipe.kt)
 * Supports swipe detection with threshold for both horizontal (month navigation)
 * and vertical (swipe down -> YearView, swipe up -> Schedule)
 */
fun Modifier.detectSwipe(
    threshold: Dp = 80.dp,
    detector: () -> ((isUp: Boolean) -> Unit),
) = this then Modifier.pointerInput(Unit) {
    val thresholdPx = threshold.toPx()
    awaitEachGesture {
        var disable = false
        var yAccumulation = 0f
        val downId = awaitFirstDown(requireUnconsumed = false).id
        val detectorInstance = detector()
        verticalDrag(downId) {
            yAccumulation += it.positionChange().y
            if (!disable && abs(yAccumulation) > thresholdPx) {
                detectorInstance(yAccumulation < 0f)
                disable = true
            }
        }
    }
}

fun Modifier.detectHorizontalSwipe(
    key1: Any = Unit,
    threshold: Dp = 80.dp,
    detector: () -> ((isLeft: Boolean) -> Unit),
) = this then Modifier.pointerInput(key1 = key1) {
    val thresholdPx = threshold.toPx()
    awaitEachGesture {
        var disable = false
        var xAccumulation = 0f
        val downId = awaitFirstDown(requireUnconsumed = false).id
        val detectorInstance = detector()
        horizontalDrag(downId) {
            xAccumulation += it.positionChange().x
            if (!disable && abs(xAccumulation) > thresholdPx) {
                it.consume()
                detectorInstance(xAccumulation < 0f)
                disable = true
            }
        }
    }
}

enum class SwipeUpAction {
    Schedule, WeekView, None
}

enum class SwipeDownAction {
    YearView, MonthView, None
}
