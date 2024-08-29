package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import otus.homework.customview.LinearChartUiState.SupportLine

class LinearChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
): View(context, attrs, defStyleAttr, defStyleRes) {

    var state: LinearChartUiState? = null
        set(value) {
            paths = value?.let { List(it.lines.size) { Path() } } ?: emptyList()
            field = value
        }
    private var paths = emptyList<Path>()

    private val gridPaint = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }
    private val linePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(40f)
        strokeWidth = 5f
    }
    private val textPaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    //    private val textHeight = ceil(-textPaint.ascent() + textPaint.descent()).toInt()

    private val format = NumberFormat.getCurrencyInstance(Locale("RU")).apply {
        currency = Currency.getInstance("RUB")
    }

    private var abscissaStep: Float = 0f
    private var ordinateStep: Float = 0f

    private var startX = 0f
    private var startY = 0f

    init {
        if (isInEditMode) {
            state = LinearChartUiState(
                row = SupportLine(
                    startValue = 1f,
                    step = 2f,
                    maxValue = 31f
                ),
                column = SupportLine(
                    startValue = 0f,
                    step = 50f,
                    maxValue = 167f,
                ),
                lines = listOf(
                    LinearChartUiState.Line(
                        category = "Здоровье",
                        color = Color.rgb(255, 191, 191),
                        points = listOf(
                            LinearChartUiState.Line.Point(
                                day = 1,
                                moneyAmount = 50.0f
                            ),
                            LinearChartUiState.Line.Point(
                                day = 2,
                                moneyAmount = 100.0f
                            ),
                            LinearChartUiState.Line.Point(
                                day = 3,
                                moneyAmount = 25.0f
                            ),
                            LinearChartUiState.Line.Point(
                                day = 4,
                                moneyAmount = 25.0f
                            ),
                        )
                    )
                )
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val state = state
            ?: throw IllegalStateException("State can't be null. Need to set state before measuring")

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        widthSize = when (widthMode) {
            MeasureSpec.UNSPECIFIED -> 200.pxToDp
            else -> max(200.pxToDp, widthSize)
        }

        heightSize = when (heightMode) {
            MeasureSpec.UNSPECIFIED -> 200.pxToDp
            else -> max(200.pxToDp, heightSize)
        }

        // FIXME: Add height and width of text
        val halfStrokeWidth = gridPaint.strokeWidth / 2

        startX = paddingLeft.toFloat()
        startY = heightSize.toFloat() - paddingTop

        val abscissaWidth = widthSize.toFloat() - paddingLeft - paddingRight - halfStrokeWidth
        abscissaStep = abscissaWidth / state.column.totalSteps

        val ordinateHeight =
            heightSize.toFloat() - paddingBottom - paddingTop - halfStrokeWidth
        ordinateStep = ordinateHeight / ceil(state.row.maxValue / state.row.step)

        val dayStep = abscissaWidth / state.column.maxValue
        val moneyStep = ordinateHeight / state.row.maxValue
        state.lines.forEachIndexed { i, line ->
            paths[i].reset()
            paths[i].moveTo(startX, startY)
            val startPathX = startX + line.points.first().day * dayStep
            paths[i].lineTo(startPathX, startY)
            for (j in 0 until line.points.size) {
                val x = startX + line.points[j].day * dayStep
                val y =
                    ordinateHeight - line.points[j].moneyAmount * moneyStep
                paths[i].lineTo(x, y)
            }
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        val state = state
            ?: throw IllegalStateException("State can't be null. Need to set state before drawing")

        var currentStep = 0
        while (state.column.step * currentStep <= state.column.maxValue) {
            canvas.drawLine(
                startX + abscissaStep * currentStep,
                startY,
                startX + abscissaStep * currentStep,
                startY - ordinateStep * state.row.totalSteps,
                gridPaint
            )
            currentStep++
        }

        currentStep = 0
        while (state.row.step * currentStep <= state.row.maxValue) {
            val y = startY - ordinateStep * currentStep
            canvas.drawLine(
                startX,
                y,
                startX + abscissaStep * state.column.totalSteps,
                y,
                gridPaint
            )
            currentStep++
        }

        paths.forEachIndexed { i, path ->
            linePaint.color = state.lines[i].color
            canvas.drawPath(path, linePaint)
        }
    }
}