package uts.sdk.modules.uniMapTencent.marker

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView

class CalloutHelper {
    companion object {
        fun createCalloutView(context: Context, model: CalloutModel): TextView {
            val arrowTextView = ArrowTextView(context, true)
            arrowTextView.setBgColor(model.bgColor ?: Color.WHITE)
            arrowTextView.setTextPadding(model.padding?.toInt() ?: 0)
            arrowTextView.setGravity(getGravity(model.textAlign))
            arrowTextView.setRadius(model.borderRadius?.toInt() ?: 0)
            arrowTextView.setStrokeWidth(model.borderWidth?.toInt() ?: 0)
            arrowTextView.setStrokeColor(model.borderColor ?: Color.TRANSPARENT)
            arrowTextView.text = model.content
            arrowTextView.includeFontPadding = false
            if (model.color != null) {
                arrowTextView.setTextColor(model.color!!)
            }
            val size: Float = model.fontSize?.toFloat() ?: 0f
            if (size > 0f) {
                arrowTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            }
            return arrowTextView
        }

        private fun getGravity(textAlign: String?): Int {
            return when (textAlign) {
                "left" -> Gravity.START
                "right" -> Gravity.END
                "center" -> Gravity.CENTER
                else -> Gravity.START
            }
        }
    }
}