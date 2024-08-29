package otus.homework.customview

import androidx.annotation.ColorInt
import kotlin.math.ceil

data class LinearChartUiState(
    val column: SupportLine,
    val row: SupportLine,
    val lines: List<Line>,
) {
    data class SupportLine(
        val startValue: Float,
        val step: Float,
        val maxValue: Float,
    ) {
        val totalSteps = ceil(maxValue / step)
    }

    data class Line(
        val category: String,
        @ColorInt
        val color: Int,
        val points: List<Point>,
    ) {
        data class Point(
            val day: Int,
            val moneyAmount: Float,
        )
    }
}
