package otus.homework.customview

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class PieChartUiState(
    val title: String,
    val date: String,
    val totalAmount: String,
    val slices: List<Slice>,
): Parcelable {

    @Parcelize
    data class Slice(
        val name: String,
        val startAngle: Float,
        val sweepAngle: Float,
        @ColorInt
        val color: Int,
    ): Parcelable
}
