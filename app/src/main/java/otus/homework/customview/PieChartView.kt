package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Parcelable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.getDimensionPixelSizeOrThrow
import kotlin.random.Random

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
): View(context, attrs, defStyleAttr, defStyleRes) {

    var state: PieChartUiState? = null

    private val headerPaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
    }
    private val datePaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
    }
    private lateinit var headerStaticLayout: StaticLayout
    private lateinit var dateStaticLayout: StaticLayout

    private val slicesRect = RectF()
    private val slicesPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f.pxToDp
    }
    private val slicesColors = listOf(
        Color.rgb(255, 191, 191),
        Color.rgb(251, 224, 174),
        Color.rgb(188, 251, 174),
        Color.rgb(187, 192, 255),
        Color.rgb(168, 230, 207),
        Color.rgb(220, 237, 193),
        Color.rgb(220, 237, 193),
        Color.rgb(255, 211, 182),
        Color.rgb(255, 170, 165),
        Color.rgb(255, 139, 148),
    )

    init {
        context.theme.obtainStyledAttributes(
            /* set = */ attrs,
            /* attrs = */ R.styleable.PieChartView,
            /* defStyleAttr = */ 0,
            /* defStyleRes = */ 0
        ).apply {
            try {
                headerPaint.textSize =
                    getDimensionPixelSize(R.styleable.PieChartView_titleFont, 16).toFloat()
                datePaint.textSize =
                    getDimensionPixelSize(R.styleable.PieChartView_dateFont, 16).toFloat()
                slicesPaint.strokeWidth =
                    getDimensionPixelSize(R.styleable.PieChartView_slicesWidth, 8).toFloat()
            } finally {
                recycle()
            }
        }

        if (isInEditMode) {
            state = PieChartUiState(
                title = "Expanses",
                date = "June 2024",
                totalAmount = "2000",
                slices = listOf(
                    PieChartUiState.Slice(
                        name = "Expense 1",
                        sweepAngle = 0.3f * 360,
                    ),
                    PieChartUiState.Slice(
                        name = "Expense 2",
                        sweepAngle = 0.25f * 360,
                    ),
                    PieChartUiState.Slice(
                        name = "Expense 3",
                        sweepAngle = 0.25f * 360,
                    ),
                    PieChartUiState.Slice(
                        name = "Expense 4",
                        sweepAngle = 0.2f * 360,
                    ),
                )
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val state = state
            ?: throw IllegalStateException("State can't be null. Need to set state before measuring")

        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val size = minOf(width, height)
        val halfStrokeWidth = slicesPaint.strokeWidth / 2

        headerStaticLayout = StaticLayout.Builder
            .obtain(
                state.title,
                0,
                state.title.length,
                headerPaint,
                size - paddingLeft - paddingRight
            )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(true)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setMaxLines(2)
            .build()

        slicesRect.set(
            paddingLeft.toFloat() + halfStrokeWidth + headerStaticLayout.height,
            paddingTop.toFloat() + halfStrokeWidth + headerStaticLayout.height,
            size.toFloat() - paddingRight - halfStrokeWidth - headerStaticLayout.height,
            size.toFloat() - paddingBottom - halfStrokeWidth - headerStaticLayout.height
        )

        dateStaticLayout = StaticLayout.Builder
            .obtain(
                state.date,
                0,
                state.date.length,
                datePaint,
                size - paddingLeft - paddingRight - slicesPaint.strokeWidth.toInt() - headerStaticLayout.height * 2
            )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(true)
            .setEllipsize(TextUtils.TruncateAt.MIDDLE)
            .setMaxLines(1)
            .build()

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val state = state
            ?: throw IllegalStateException("State can't be null. Need to set state before drawing")

        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        headerStaticLayout.draw(canvas)
        canvas.restore()

        var startAngle = -90f
        state.slices.forEachIndexed { index, slice: PieChartUiState.Slice ->
            slicesPaint.color = slicesColors.getOrElse(index) { slicesColors[Random.nextInt(0, 9)] }
            canvas.drawArc(slicesRect, startAngle, slice.sweepAngle, false, slicesPaint)
            startAngle += slice.sweepAngle
        }

        canvas.save()
        canvas.translate(
            paddingLeft + headerStaticLayout.height + slicesRect.width() / 2f - dateStaticLayout.width / 2 + slicesPaint.strokeWidth / 2,
            paddingTop + headerStaticLayout.height - dateStaticLayout.height / 2 + slicesRect.height() / 2 + slicesPaint.strokeWidth / 2
        )
        dateStaticLayout.draw(canvas)
        canvas.restore()
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
        bundle.putParcelable(KEY_SAVE_DATA, state)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            return if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                this.state = state.getParcelable(KEY_SAVE_DATA, PieChartUiState::class.java)
                super.onRestoreInstanceState(
                    state.getParcelable(
                        KEY_SUPER_STATE,
                        Parcelable::class.java
                    )
                )
            } else {
                this.state = state.getParcelable(KEY_SAVE_DATA)
                super.onRestoreInstanceState(state.getParcelable(KEY_SUPER_STATE))
            }
        }
        super.onRestoreInstanceState(state)
    }

    companion object {

        private const val KEY_SAVE_DATA = "KEY_PIE_CHART_VIEW_DATA"
        private const val KEY_SUPER_STATE = "KEY_SUPER_STATE"
    }
}