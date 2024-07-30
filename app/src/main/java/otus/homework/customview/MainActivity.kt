package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.text.NumberFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.Currency
import java.util.Locale
import kotlinx.serialization.json.Json
import otus.homework.customview.PieChartUiState.Slice

class MainActivity: AppCompatActivity() {

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
                val pieChartSlices = transactions.map { transaction ->
                    Slice(
                        name = transaction.name,
                        sweepAngle = (transaction.amount / totalAmount) * 360,
                    )
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