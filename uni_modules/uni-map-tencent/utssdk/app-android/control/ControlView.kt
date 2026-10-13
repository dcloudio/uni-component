package uts.sdk.modules.uniMapTencent.control

import android.graphics.BitmapFactory
import android.widget.FrameLayout
import android.widget.ImageView
import uts.sdk.modules.uniMapTencent.FrescoImageLoader
import uts.sdk.modules.uniMapTencent.ICallBack

class ControlView(control: Control, private val frameLayout: FrameLayout, private val callback: ICallBack) {
    private var imageView = ImageView(frameLayout.context)
    private var layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )

    init {
        layoutParams.leftMargin = control.position.left?.toInt() ?: 0
        layoutParams.topMargin = control.position.top?.toInt() ?: 0
        layoutParams.width = control.position.width?.toInt() ?: FrameLayout.LayoutParams.WRAP_CONTENT
        layoutParams.height = control.position.height?.toInt() ?: FrameLayout.LayoutParams.WRAP_CONTENT

        if (control.clickable == true) {
            imageView.setOnClickListener {
                callback.callback(
                    mapOf(
                        "type" to "controltap",
                        "detail" to mapOf("controlId" to if (control.id == null) control.hashCode() else control.id)
                    )
                )
            }
        }

        if (control.iconPath.startsWith("file:///android_asset/")) {
            val path = control.iconPath.replace("file:///android_asset/", "")
            try {
                val bitmap = BitmapFactory.decodeStream(frameLayout.context.assets.open(path))
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
            }
        } else {
            FrescoImageLoader.loadBitmap(
                frameLayout.context,
                control.iconPath,
                null,
                null,
                { bitmap -> imageView.setImageBitmap(bitmap) },
                {}
            )
        }
    }


    fun attachView() {
        frameLayout.addView(imageView, layoutParams)
    }

    fun detachView() {
        frameLayout.removeView(imageView)
    }
}
