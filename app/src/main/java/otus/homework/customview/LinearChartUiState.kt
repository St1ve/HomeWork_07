package otus.homework.customview

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlin.math.ceil
import kotlinx.parcelize.Parcelize

@Parcelize
data class LinearChartUiState(
    val column: SupportLine,
    val row: SupportLine,
    val lines: List<Line>,
): Parcelable {

    @Parcelize
    data class SupportLine(
        val startValue: Float,
        val step: Float,
        val maxValue: Float,
    ): Parcelable {

        val totalSteps = ceil(maxValue / step)
    }

    @Parcelize
    data class Line(
        val category: String,
        @ColorInt
        val color: Int,
        val points: List<Point>,
    ): Parcelable {

        @Parcelize
        data class Point(
            val day: Int,
            val moneyAmount: Float,
        ): Parcelable
    }
}
