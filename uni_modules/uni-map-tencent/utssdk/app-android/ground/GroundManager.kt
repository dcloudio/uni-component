package uts.sdk.modules.uniMapTencent.ground

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.GroundOverlay
import com.tencent.tencentmap.mapsdk.maps.model.GroundOverlayOptions
import uts.sdk.modules.uniMapTencent.ICallBack

class GroundManager(private val context: Context, val map: TencentMap) {
    private var mGroundCaches = mutableMapOf<String, GroundOverlay>()

    fun addGroundOverlay(ground: Ground, iCallBack: ICallBack) {
        updateGroundOverlay(ground, iCallBack)
    }

    fun updateGroundOverlay(ground: Ground, iCallBack: ICallBack) {
        var isAddToMap = true
        if (mGroundCaches.containsKey(ground.id)) {
            isAddToMap = false
        }
        try {
            if (isAddToMap) {
                val groundOverlayOptions = GroundOverlayOptions()
                configGroundInstance(groundOverlayOptions, ground, iCallBack, object : ICallBack {
                    override fun callback(param: Any?) {
                        val groundOverlay = map.addGroundOverlay(groundOverlayOptions)
                        mGroundCaches[ground.id] = groundOverlay
                        iCallBack.callback(
                            mapOf(
                                "type" to "success",
                                "errMsg" to ""
                            )
                        )
                    }
                })

            } else {
                val current = mGroundCaches[ground.id]
                configGroundInstance(current!!, ground, iCallBack, object : ICallBack {
                    override fun callback(param: Any?) {
                        iCallBack.callback(
                            mapOf(
                                "type" to "success",
                                "errMsg" to ""
                            )
                        )
                    }
                })
            }
        } catch (e: Exception) {
            iCallBack.callback(
                mapOf(
                    "type" to "fail",
                    "errMsg" to e.message
                )
            )
        }
    }


    /**
     * 配置GroundOverlayOptions或者GroundOverlay
     * @param groundInstance Any
     * @param ground Ground
     */
    private fun configGroundInstance(groundInstance: Any, ground: Ground, iCallBack: ICallBack, completeCallback: ICallBack?) {
        if (groundInstance is GroundOverlayOptions) {
            groundInstance.visible(ground.visible)
            groundInstance.alpha(ground.opacity.toFloat())
            groundInstance.zIndex(ground.zIndex.toInt())
            groundInstance.latLngBounds(ground.bounds.convertBounds())
            if (ground.src.startsWith("file:///android_asset/")) {
                val path = ground.src.replace("file:///android_asset/", "")
                val descriptor = BitmapDescriptorFactory.fromAsset(path)
                groundInstance.bitmap(descriptor)
                completeCallback?.callback(null)
            } else if (ground.src.startsWith("file://")) {
                val descriptor = BitmapDescriptorFactory.fromPath(ground.src)
                groundInstance.bitmap(descriptor)
                completeCallback?.callback(null)
            } else {
                //网络图片地址
                Glide.with(context)
                    .asBitmap()
                    .load(ground.src)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            groundInstance.bitmap(BitmapDescriptorFactory.fromBitmap(resource))
                            completeCallback?.callback(null)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            groundInstance.bitmap(null)
                            iCallBack.callback(
                                mapOf(
                                    "type" to "fail",
                                    "errMsg" to "network image loading failed"
                                )
                            )
                        }
                    })
            }
        } else if (groundInstance is GroundOverlay) {
            groundInstance.setVisibility(ground.visible)
            groundInstance.setAlpha(ground.opacity.toFloat())
            groundInstance.setZindex(ground.zIndex.toInt())
            groundInstance.setLatLongBounds(ground.bounds.convertBounds())
            if (ground.src.startsWith("file:///android_asset/")) {
                val path = ground.src.replace("file:///android_asset/", "")
                val descriptor = BitmapDescriptorFactory.fromAsset(path)
                groundInstance.setBitmap(descriptor)
                completeCallback?.callback(null)
            } else if (ground.src.startsWith("file://")) {
                val descriptor = BitmapDescriptorFactory.fromPath(ground.src)
                groundInstance.setBitmap(descriptor)
                completeCallback?.callback(null)
            } else {
                //网络图片地址
                Glide.with(context)
                    .asBitmap()
                    .load(ground.src)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            groundInstance.setBitmap(BitmapDescriptorFactory.fromBitmap(resource))
                            completeCallback?.callback(null)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            groundInstance.setBitmap(null)
                            iCallBack.callback(
                                mapOf(
                                    "type" to "fail",
                                    "errMsg" to "network image loading failed"
                                )
                            )
                        }
                    })
            }
        }
    }


    fun removeGroundOverlay(id: String, iCallBack: ICallBack) {
        try {
            if (mGroundCaches.containsKey(id)) {
                mGroundCaches.remove(id)?.remove()
            } else {
                iCallBack.callback(mapOf(
                    "type" to "fail",
                    "errMsg" to "id not found"
                ))
                return
            }
            iCallBack.callback(
                mapOf(
                    "type" to "success",
                    "errMsg" to ""
                )
            )
        } catch (e: Exception) {
            iCallBack.callback(
                mapOf(
                    "type" to "fail",
                    "errMsg" to e.message
                )
            )
        }
    }


    fun destroy() {
        mGroundCaches.forEach { (_, u) ->
            u.remove()
        }
        mGroundCaches.clear()
    }
}