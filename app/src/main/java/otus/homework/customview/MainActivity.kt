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
import kotlin.math.max
import kotlinx.serialization.json.Json
import otus.homework.customview.LinearChartUiState.SupportLine
import otus.homework.customview.PieChartUiState.Slice

class MainActivity: AppCompatActivity() {

    private val colors = listOf(
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
        val linearChartView = findViewById<LinearChartView>(R.id.linear_chart_view)

        if (savedInstanceState == null) {
            val payloadRaw = resources.openRawResource(R.raw.payload).bufferedReader().use {
                it.readText()
            }
            val transactions = Json.decodeFromString<List<Transaction>>(payloadRaw)
            val groupedByDateTransaction = transactions
                .groupBy { transaction ->
                    val instant = Instant
                        .ofEpochSecond(transaction.time)
                        .atZone(ZoneId.systemDefault())

                    YearMonth.from(instant)
                }

            val pieChartUiStates = mapTransactionsToPieChartUiState(groupedByDateTransaction)
            pieChartView.state = pieChartUiStates.first()

            val linearChartUiStates = mapTransactionsToLinearChartUiState(groupedByDateTransaction)
            linearChartView.state = linearChartUiStates.first()
        }
    }

    private fun mapTransactionsToPieChartUiState(
        groupedByDateTransaction: Map<YearMonth, List<Transaction>>,
    ): List<PieChartUiState> {
        val format = NumberFormat.getCurrencyInstance(Locale("RU"))
        format.currency = Currency.getInstance("RUB")

        return groupedByDateTransaction
            .map { (yearMonth, transactions) ->
                val totalAmount = transactions.sumOf { it.amount }.toFloat()
                var startAngle = -90f
                var index = 0

                val pieChartSlices = transactions
                    .groupBy { transaction -> transaction.category }
                    .map { (category, categoryTransactions) ->
                        val sweepAngle =
                            (categoryTransactions.sumOf { it.amount } / totalAmount) * 360
                        Slice(
                            name = category,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            color = colors.getOrElse(index) { colors[index % 9] }
                        ).also {
                            index++
                            startAngle += sweepAngle
                        }
                    }

                PieChartUiState(
                    title = "Expenses",
                    date = yearMonth.toString(),
                    totalAmount = format.format(totalAmount),
                    slices = pieChartSlices,
                )
            }
    }

    private fun mapTransactionsToLinearChartUiState(
        groupedByDateTransaction: Map<YearMonth, List<Transaction>>,
    ): List<LinearChartUiState> {
        return groupedByDateTransaction
            .map { (yearMonth, transactions) ->
                val maxMoneyAmount = transactions.maxOf { it.amount }.toFloat()

                var index = 0
                val lines = transactions
                    .groupBy { transaction -> transaction.category }
                    .map { (category, transactions) ->
                        LinearChartUiState.Line(
                            category = category,
                            color = colors.getOrElse(index) { colors[index % 9] },
                            points = transactions.map { transaction ->
                                val instant = Instant
                                    .ofEpochSecond(transaction.time)
                                    .atZone(ZoneId.systemDefault())

                                LinearChartUiState.Line.Point(
                                    day = instant.dayOfMonth,
                                    moneyAmount = transaction.amount.toFloat()
                                )
                            }
                        ).also { index++ }
                    }
                val roundedMaxMoney = ((maxMoneyAmount + 100) / 100).toInt() * 100f
                LinearChartUiState(
                    column = SupportLine(
                        startValue = 1f,
                        step = 2f,
                        maxValue = yearMonth.month.maxLength().toFloat(),
                    ),
                    row = SupportLine(
                        startValue = 0f,
                        step = roundedMaxMoney / 5,
                        maxValue = roundedMaxMoney,
                    ),
                    lines = lines
                )
            }
    }
}