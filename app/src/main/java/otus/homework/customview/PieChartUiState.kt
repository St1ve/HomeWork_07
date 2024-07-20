package otus.homework.customview

data class PieChartUiState(
    val title: String,
    val date: String,
    val totalAmount: String,
    val slices: List<Slice>,
) {

    data class Slice(
        val name: String,
        val percentage: Float,
    )
}
