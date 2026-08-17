package uts.sdk.modules.uniMapTencent.marker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.widget.TextView

@SuppressLint("AppCompatCustomView")
class ArrowTextView(context: Context, isSharp: Boolean) : TextView(context) {
    private var mRadius = 0
    private var mTextBgColor = Color.BLACK
    private var defPadding = 10
    private var mStrokeWidth = 0
    private var mPadding = 0
    private var mStrokeColor = Color.TRANSPARENT
    private var isSharp = true

    init {
        this.isSharp = isSharp
        if (!isSharp) {
            defPadding = 0
        }
    }

    fun setRadius(r: Int) {
        this.mRadius = r
        invalidate()
    }

    fun setBgColor(color: Int) {
        mTextBgColor = color
        invalidate()
    }

    fun setTextPadding(padding: Int) {
        mPadding = padding
        val p = mPadding + defPadding + mStrokeWidth
        setPadding(p, p, p, p)
        invalidate()
    }

    fun setStrokeWidth(strokeWidth: Int) {
        this.mStrokeWidth = strokeWidth
        setTextPadding(mPadding)
    }

    override fun onDraw(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true //设置画笔抗锯齿

        val height = height //获取View的高度
        val width = width //获取View的宽度

        //框定文本显示的区域
        val contentPadding = defPadding + mStrokeWidth
        val contentRect = RectF(contentPadding.toFloat(), contentPadding.toFloat(), (width - contentPadding).toFloat(), (height - contentPadding).toFloat())
        if (mStrokeWidth > 0) { //描边内容区域
            val rectStroke = RectF(defPadding.toFloat(), defPadding.toFloat(), (width - defPadding).toFloat(), (height - defPadding).toFloat())
            //设置线宽
            paint.color = mStrokeColor
            canvas.drawRoundRect(rectStroke, mRadius.toFloat(), mRadius.toFloat(), paint)
        }

        paint.color = mTextBgColor
        canvas.drawRoundRect(contentRect, mRadius.toFloat(), mRadius.toFloat(), paint)

        if (isSharp) {
            if (mStrokeWidth > 0) { //描边箭头
                val pathStrok = Path()
                //以下是绘制文本的那个箭头
                pathStrok.moveTo((width / 2).toFloat(), height.toFloat()) // 三角形顶点
                pathStrok.lineTo((width / 2 - defPadding).toFloat(), (height - defPadding).toFloat()) //三角形左边的点
                pathStrok.lineTo((width / 2 + defPadding).toFloat(), (height - defPadding).toFloat()) //三角形右边的点
                paint.color = mStrokeColor
                pathStrok.close()
                canvas.drawPath(pathStrok, paint)
            }

            val path = Path()
            path.moveTo((width / 2).toFloat(), (height - mStrokeWidth).toFloat()) // 三角形顶点
            path.lineTo((width / 2 - defPadding).toFloat(), (height - contentPadding).toFloat()) //三角形左边的点
            path.lineTo((width / 2 + defPadding).toFloat(), (height - contentPadding).toFloat()) //三角形右边的点
            paint.color = mTextBgColor
            path.close()
            canvas.drawPath(path, paint)
        }
        super.onDraw(canvas)
    }

    fun setStrokeColor(strokeColor: Int) {
        this.mStrokeColor = strokeColor
    }
}