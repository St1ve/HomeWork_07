package otus.homework.customview

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.text.NumberFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.Currency
import java.util.Locale
import kotlin.random.Random
import kotlinx.serialization.json.Json
import otus.homework.customview.PieChartUiState.Slice

class MainActivity: AppCompatActivity() {

    private val slicesColors = listOf(
        Color.rgb(255, 191, 191),
        Color.rgb(251, 224, 174),
        Color.rgb(188, 251, 174),
        Color.rgb(187, 192, 255),
        Color.rgb(168, 230, 207),
        Color.rgb(220, 237, 193),
        Color.rgb(214, 246, 221),
        Color.rgb(255, 211, 182),
        Color.rgb(255, 170, 165),
        Color.rgb(255, 139, 148),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChartView = findViewById<PieChartView>(R.id.pie_chart_view)

        if (savedInstanceState == null) {
            val payloadRaw = resources.openRawResource(R.raw.payload).bufferedReader().use {
                it.readText()
            }
            val transactions = Json.decodeFromString<List<Transaction>>(payloadRaw)

            val pieChartUiState = mapTransactionsToPieChartUiState(transactions)
            pieChartView.state = pieChartUiState.first()
        }
    }

    private fun mapTransactionsToPieChartUiState(
        transactions: List<Transaction>,
    ): List<PieChartUiState> {
        val format = NumberFormat.getCurrencyInstance(Locale("RU"))
        format.currency = Currency.getInstance("RUB")

        return transactions
            .groupBy { transaction ->
                val instant = Instant
                    .ofEpochSecond(transaction.time)
                    .atZone(ZoneId.systemDefault())

                YearMonth.from(instant)
            }
            .map { (yearMonth, transactions) ->
                val totalAmount = transactions.sumOf { it.amount }.toFloat()
                var startAngle = -90f
                val pieChartSlices = transactions.mapIndexed { i, transaction ->
                    val sweepAngle = (transaction.amount / totalAmount) * 360
                    Slice(
                        name = transaction.name,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        color = slicesColors.getOrElse(i) { slicesColors[i % 9] }
                    ).also { startAngle += sweepAngle }
                }

                PieChartUiState(
                    title = "Expenses",
                    date = yearMonth.toString(),
                    totalAmount = format.format(totalAmount),
                    slices = pieChartSlices,
                )
            }
    }
}