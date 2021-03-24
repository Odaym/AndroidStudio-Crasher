package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import kotlin.math.min
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class OnlineBuyingBreadcrumbsView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val breadcrumbsCount =9
    private var selectedBreadcrumb = -1

    private val selectedBluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ResourcesCompat.getColor(resources, R.color.white, null)
    }
    private val unselectedBluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ResourcesCompat.getColor(resources, R.color.white, null)
    }
    private val completedGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ResourcesCompat.getColor(resources, R.color.white, null)
    }

    private val breadcrumbsX = FloatArray(breadcrumbsCount)
    private var selectedBreadcrumbRadius = 0f
    private var unselectedBreadcrumbRadius = 0f

    private val horizontalLineRect = RectF()

    private val isRtl by lazy { layoutDirection == LAYOUT_DIRECTION_RTL }

    private var checkBitmap: Bitmap? = null
    private var checkBitmapRadius = 0f

    private val checkRect = RectF()

    override fun onMeasure(w: Int, h: Int) {
        super.onMeasure(w, h)

        selectedBreadcrumbRadius = h * 0.5f
        unselectedBreadcrumbRadius = selectedBreadcrumbRadius * 0.75f

        val stepSpan = w.toFloat() / breadcrumbsCount

        var x = stepSpan * .5f
        for (i in 0 until breadcrumbsCount) {
            breadcrumbsX[i] = x
            x += stepSpan
        }

        val lineHeight = h * 0.2f
        val lineTop = (h - lineHeight) / 2
        val lineBottom = lineTop + lineHeight
        horizontalLineRect.set(breadcrumbsX.first(), lineTop, breadcrumbsX.last(), lineBottom)

        val checkSize = (selectedBreadcrumbRadius * 1.5f).roundToInt()
        makeCheckBitmap(checkSize)
    }

    private fun makeCheckBitmap(size: Int) {
        checkBitmap?.recycle()

        checkBitmapRadius = size * .5f
        checkBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(checkBitmap!!)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ResourcesCompat.getColor(resources, android.R.color.white, null)
        }
        val path = Path()
        // viewport size 24 -> android:pathData="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z"
        val points = listOf(
            9f to 16.17f,
            4.83f to 12f,
            4.83f - 1.42f to 12f + 1.41f,
            9f to 19f,
            21f to 7f,
            21f - 1.41f to 7f - 1.41f
        )
        val scaleFactor = size / 24f
        points.forEachIndexed { index, (x, y) ->
            val actualX = x * scaleFactor
            val actualY = y * scaleFactor
            if (index == 0) {
                path.moveTo(actualX, actualY)
            } else {
                path.lineTo(actualX, actualY)
            }
        }

        if (isRtl) {
            canvas.save()
            canvas.scale(-1f, 1f, size * .5f, size * .5f)
        }

        canvas.drawPath(path, paint)

        if (isRtl) {
            canvas.restore()
        }
    }

    fun setStep(step: Int) {
        selectedBreadcrumb = step
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isRtl) {
            canvas.save()
            canvas.scale(-1f, 1f, width * .5f, height * .5f)
        }

        with(horizontalLineRect) {
            set(breadcrumbsX.first(), top, breadcrumbsX.last(), bottom)
        }
        canvas.drawRect(horizontalLineRect, unselectedBluePaint)

        val selectedDotIndex = min(selectedBreadcrumb, breadcrumbsX.size - 1)
        if (selectedDotIndex > 0) {
            horizontalLineRect.setRight(breadcrumbsX[selectedDotIndex])
            canvas.drawRect(horizontalLineRect, selectedBluePaint)
        }

        val completedDotIndex = min(selectedBreadcrumb - 1, breadcrumbsX.size - 1)
        if (completedDotIndex > 0) {
            horizontalLineRect.setRight(breadcrumbsX[completedDotIndex])
            canvas.drawRect(horizontalLineRect, completedGreenPaint)
        }

        breadcrumbsX.forEachIndexed { index, dotX ->
            val radius = if (index <= selectedBreadcrumb) {
                selectedBreadcrumbRadius
            } else {
                unselectedBreadcrumbRadius
            }
            val isCompletedStep = selectedBreadcrumb > index
            val isSelectedStep = selectedBreadcrumb == index
            val paint = when {
                isCompletedStep -> completedGreenPaint
                isSelectedStep -> selectedBluePaint
                else -> unselectedBluePaint
            }
            val cy = height / 2f
            canvas.drawCircle(dotX, cy, radius, paint)

            if (isCompletedStep && checkBitmap != null) {
                checkRect.set(
                    dotX - checkBitmapRadius,
                    cy - checkBitmapRadius,
                    dotX + checkBitmapRadius,
                    cy + checkBitmapRadius
                )
                canvas.drawBitmap(checkBitmap!!, null, checkRect, null)
            }
        }

        if (isRtl) {
            canvas.restore()
        }
    }

    private fun RectF.setRight(right: Float) = set(this.left, this.top, right, this.bottom)

    companion object {
        const val NO_STEP = -1
    }
}