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
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
): View(context, attrs, defStyleAttr, defStyleRes) {

    var state: PieChartUiState? = null
        set(value) {
            clickedSliceIndexed = null
            field = value
        }
    private var clickedSliceIndexed: Int? = null

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
    private lateinit var clickedSliceStaticLayout: StaticLayout

    private val slicesRect = RectF()
    private val slicesPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f.dpToPx
    }
    private val selectedSlicePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f.dpToPx
    }

    private val selectedSliceTextPaint = TextPaint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

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
                selectedSlicePaint.strokeWidth = slicesPaint.strokeWidth * 1.25f
                selectedSliceTextPaint.textSize =
                    getDimensionPixelSize(
                        R.styleable.PieChartView_selectedSliceTextFont,
                        8
                    ).toFloat()
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
                        startAngle = -90f,
                        sweepAngle = 0.3f * 360,
                        color = Color.rgb(255, 191, 191),
                    ),
                    PieChartUiState.Slice(
                        name = "Expense 2",
                        startAngle = -90f + 0.3f * 360,
                        sweepAngle = 0.25f * 360,
                        color = Color.rgb(251, 224, 174),
                    ),
                    PieChartUiState.Slice(
                        name = "Expense 3",
                        startAngle = -90f + 0.25f * 360 + 0.3f * 360,
                        sweepAngle = 0.25f * 360,
                        color = Color.rgb(188, 251, 174),
                    ),
                    PieChartUiState.Slice(
                        name = "Expense 4",
                        startAngle = -90f + 0.25f * 360f + 0.3f * 360f + 0.25f * 360f,
                        sweepAngle = 0.2f * 360,
                        Color.rgb(187, 192, 255),
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

        state.slices.forEachIndexed { index, slice ->
            if (index == clickedSliceIndexed) {
                selectedSlicePaint.color = slice.color
                canvas.drawArc(slicesRect, slice.startAngle, slice.sweepAngle, false, selectedSlicePaint)

                canvas.save()
                canvas.translate(
                    paddingLeft + headerStaticLayout.height + slicesRect.width() / 2f - clickedSliceStaticLayout.width / 2 + slicesPaint.strokeWidth / 2,
                    paddingTop + headerStaticLayout.height - dateStaticLayout.height / 2 + slicesRect.height() / 2 + slicesPaint.strokeWidth / 2 + dateStaticLayout.height
                )
                clickedSliceStaticLayout.draw(canvas)
                canvas.restore()
            } else {
                slicesPaint.color = slice.color
                canvas.drawArc(slicesRect, slice.startAngle, slice.sweepAngle, false, slicesPaint)
            }
        }

        canvas.save()
        canvas.translate(
            paddingLeft + headerStaticLayout.height + slicesRect.width() / 2f - dateStaticLayout.width / 2 + slicesPaint.strokeWidth / 2,
            paddingTop + headerStaticLayout.height - dateStaticLayout.height / 2 + slicesRect.height() / 2 + slicesPaint.strokeWidth / 2
        )
        dateStaticLayout.draw(canvas)
        canvas.restore()
    }

    private val sliceGestureDetector = GestureDetector(context, object: SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val state = state
                ?: throw IllegalStateException("State can't be null. Need to set state before measuring")

            val angle = Math
                .toDegrees(atan2(e.y - slicesRect.centerY(), e.x - slicesRect.centerX()).toDouble())
                .toFloat()
            val clickedDegrees = if (angle <= -90) {
                angle + 360f
            } else {
                angle
            }

            val clickedIndex = state.slices.indexOfFirst { slice ->
                slice.startAngle <= clickedDegrees && (slice.startAngle + slice.sweepAngle) > clickedDegrees
            }
            if (clickedIndex != clickedSliceIndexed) {
                val clickedSliceName = state.slices[clickedIndex].name
                clickedSliceStaticLayout = StaticLayout.Builder
                    .obtain(
                        clickedSliceName,
                        0,
                        clickedSliceName.length,
                        selectedSliceTextPaint,
                        dateStaticLayout.width
                    )
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, 1f)
                    .setIncludePad(true)
                    .setEllipsize(TextUtils.TruncateAt.MIDDLE)
                    .setMaxLines(1)
                    .build()

                clickedSliceIndexed = clickedIndex
                invalidate()
            }

            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return sliceGestureDetector.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
        bundle.putParcelable(KEY_SAVE_STATE, state)
        clickedSliceIndexed?.let { bundle.putInt(KEY_SAVE_CLICKED_INDEX, it) }
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            return if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                this.state = state.getParcelable(KEY_SAVE_STATE, PieChartUiState::class.java)
                val clickedIndex = state.getInt(KEY_SAVE_CLICKED_INDEX, Int.MIN_VALUE)
                if (clickedIndex != Int.MIN_VALUE) {
                    clickedSliceIndexed = clickedIndex
                }
                super.onRestoreInstanceState(
                    state.getParcelable(
                        KEY_SUPER_STATE,
                        Parcelable::class.java
                    )
                )
            } else {
                this.state = state.getParcelable(KEY_SAVE_STATE)
                super.onRestoreInstanceState(state.getParcelable(KEY_SUPER_STATE))
            }
        }
        super.onRestoreInstanceState(state)
    }

    companion object {

        private const val KEY_SAVE_CLICKED_INDEX = "KEY_PIE_CHART_VIEW_CLICKED_INDEX"
        private const val KEY_SAVE_STATE = "KEY_PIE_CHART_VIEW_STATE_DATA"
        private const val KEY_SUPER_STATE = "KEY_SUPER_STATE"
    }
}