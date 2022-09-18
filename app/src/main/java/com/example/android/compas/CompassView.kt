package com.example.android.compas

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat


class CompassView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {
    private var markerPaint: Paint
    private var textPaint: Paint
    private var circlePaint: Paint
    private var northString: String
    private var eastString: String
    private var southString: String
    private var westString: String
    private var textHeight: Int

    var bearing: Float = 0f
        set(value) {
            field = value
            invalidate()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED)
        }

    constructor(context: Context): this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet): this(context, attrs, 0)

    init {
        isFocusable = true
        val attributes: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CompassView,
            defStyleAttr,
            0
        )

        if (attributes.hasValue(R.styleable.CompassView_bearing)) {
            bearing = attributes.getFloat(R.styleable.CompassView_bearing, 0f)
        }
        attributes.recycle()
        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.color = ContextCompat.getColor(
            context,
            R.color.background_color
        )
        circlePaint.strokeWidth = 1f
        circlePaint.style = Paint.Style.FILL_AND_STROKE
        northString = resources.getString(R.string.cardinal_north)
        eastString = resources.getString(R.string.cardinal_east)
        southString = resources.getString(R.string.cardinal_south)
        westString = resources.getString(R.string.cardinal_west)
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = ContextCompat.getColor(context, R.color.text_color)
        textHeight = textPaint.measureText("yY").toInt()
        markerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        markerPaint.color = ContextCompat.getColor(
            context,
            R.color.marker_color
        )
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureWidth = measure(widthMeasureSpec)
        val measureHeight = measure(heightMeasureSpec)
        val d = measureWidth.coerceAtMost(measureHeight)

        setMeasuredDimension(d, d)
    }

    private fun measure(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        val result = if (specMode == MeasureSpec.UNSPECIFIED) {
            200
        } else {
            specSize
        }

        return result
    }

    override fun onDraw(canvas: Canvas) {
        val mMeasuredWidth = measuredWidth
        val mMeasuredHeight = measuredHeight
        val px = mMeasuredWidth / 2f
        val py = mMeasuredHeight / 2f
        val radius = px.coerceAtMost(py)

        canvas.drawCircle(px, py, radius, circlePaint)
        canvas.save()
        canvas.rotate(-bearing, px, py)
        val textWidth = textPaint.measureText("W").toInt()
        val cardinalX = (px - textWidth / 2).toInt()
        val cardinalY = (py - radius + textHeight).toInt()
        for (i in 0..23) {
            canvas.drawLine(
                px, py - radius, px, py - radius + 10,
                markerPaint
            )
            canvas.save()
            canvas.translate(0F, textHeight.toFloat())
            if (i % 6 == 0) {
                var dirString = ""
                when (i) {
                    0 -> {
                        dirString = northString
                        val arrowY = 2 * textHeight
                        canvas.drawLine(
                            px, arrowY.toFloat(), px - 5,
                            (3 * textHeight).toFloat(),
                            markerPaint
                        )
                        canvas.drawLine(
                            px, arrowY.toFloat(), px + 5,
                            (3 * textHeight).toFloat(),
                            markerPaint
                        )
                    }
                    6 -> dirString = eastString
                    12 -> dirString = southString
                    18 -> dirString = westString
                }
                canvas.drawText(
                    dirString, cardinalX.toFloat(), cardinalY.toFloat(),
                    textPaint
                )
            } else if (i % 3 == 0) {
                val angle = (i * 15).toString()
                val angleTextWidth = textPaint.measureText(angle)
                val angleTextX = (px - angleTextWidth / 2).toInt()
                val angleTextY = (py - radius + textHeight).toInt()
                canvas.drawText(
                    angle, angleTextX.toFloat(), angleTextY.toFloat(),
                    textPaint
                )
            }
            canvas.restore()
            canvas.rotate(15F, px, py)
        }
        canvas.restore()

    }

    override fun dispatchPopulateAccessibilityEvent(
        event: AccessibilityEvent
    ): Boolean {
        super.dispatchPopulateAccessibilityEvent(event)
        return if (isShown) {
            val bearingStr: String = bearing.toString()
            event.text.add(bearingStr)
            true
        } else false
    }
 }