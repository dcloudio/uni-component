package uts.sdk.modules.uniMapTencent

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder

object FrescoImageLoader {
    fun loadBitmap(
        context: Context,
        path: String,
        width: Int?,
        height: Int?,
        onSuccess: (Bitmap) -> Unit,
        onFailure: () -> Unit
    ) {
        initialize(context)
        val source = if (path.startsWith("/")) Uri.fromFile(File(path)) else Uri.parse(path)
        val requestBuilder = ImageRequestBuilder.newBuilderWithSource(source)
        if (width != null && height != null) {
            requestBuilder.setResizeOptions(ResizeOptions(width, height))
        }
        val dataSource = Fresco.getImagePipeline().fetchDecodedImage(requestBuilder.build(), null)
        dataSource.subscribe(
            object : BaseBitmapDataSubscriber() {
                override fun onNewResultImpl(bitmap: Bitmap?) {
                    if (bitmap == null) {
                        onFailure()
                    } else {
                        onSuccess(bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, false))
                    }
                }

                override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                    onFailure()
                }
            },
            UiThreadImmediateExecutorService.getInstance()
        )
    }

    private fun initialize(context: Context) {
        synchronized(this) {
            if (!Fresco.hasBeenInitialized()) {
                Fresco.initialize(context)
            }
        }
    }
}
