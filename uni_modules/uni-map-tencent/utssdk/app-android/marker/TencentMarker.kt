package uts.sdk.modules.uniMapTencent.marker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tencent.tencentmap.mapsdk.maps.MapView
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.Marker
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions
import com.tencent.tencentmap.mapsdk.vector.utils.animation.MarkerTranslateAnimator
import uts.sdk.modules.uniMapTencent.ICallBack


class TencentMarker(val markerModel: MarkerModel, val mapView: MapView) : IUniMarker {
    private var mMarker: Marker? = null
    private var cacheCalloutView: View? = null

    init {
        createMarkerOptions(markerModel, object : ICallBack {
            override fun callback(param: Any?) {
                if (param is MarkerOptions) {
                    mMarker = mapView.map.addMarker(param)
                    //显示infowindow会带来一些列查找的问题，所以需要在Marker存储完毕后再进行显示
                    Handler(Looper.getMainLooper()).post {
                        if (isAlwaysDisPlay()) {
                            mMarker?.showInfoWindow()
                        }
                    }
                }
            }
        })
    }

    private fun createMarkerOptions(markerModel: MarkerModel, completeCallback: ICallBack) {
        createBitmapDescriptor(markerModel.iconPath, markerModel.width?.toInt(), markerModel.height?.toInt(), object : ICallBack {
            override fun callback(param: Any?) {
                val markerOptions = MarkerOptions(LatLng(markerModel.latitude, markerModel.longitude))
                markerOptions.title(markerModel.title)
                markerOptions.rotation(markerModel.rotate.toFloat())
                markerOptions.alpha(markerModel.alpha)
                markerOptions.anchor(markerModel.anchor[0], markerModel.anchor[1])
                markerOptions.infoWindowEnable(true)
                if (param is BitmapDescriptor) {
                    markerOptions.icon(param)
                }
                completeCallback.callback(markerOptions)
            }
        })
    }

    fun updateMarkerOptions(markerModel: MarkerModel) {
        releaseCalloutView()
        createBitmapDescriptor(markerModel.iconPath, markerModel.width?.toInt(), markerModel.height?.toInt(), object : ICallBack {
            override fun callback(param: Any?) {
                mMarker?.position = LatLng(markerModel.latitude, markerModel.longitude)
                mMarker?.title = markerModel.title
                mMarker?.rotation = markerModel.rotate.toFloat()
                mMarker?.alpha = markerModel.alpha
                mMarker?.setAnchor(markerModel.anchor[0], markerModel.anchor[1])
                if (param is BitmapDescriptor) {
                    mMarker?.setIcon(param)
                }
            }
        })
    }

    fun translateMarker(
        destination: uts.sdk.modules.uniMapTencent.latlng.LatLng,
        rotate: Double?,
        moveWithRotate: Boolean?,
        duration: Double?,
        callback: ICallBack
    ) {
        // TODO: 需要考虑callout和label
        val dest = LatLng(destination.latitude, destination.longitude)
        if (rotate != null) {
            rotateAndTranslateAnimation(mMarker!!, rotate.toFloat(), duration?.toLong() ?: 1000, dest, moveWithRotate ?: false)
        } else {
            translateAnimation(dest, duration?.toLong() ?: 1000, mMarker!!)
        }
        callback.callback(
            mapOf(
                "type" to "success",
                "errMsg" to ""
            )
        )
    }

    fun getCalloutView(): View? {
        if (markerModel.callout == null) return null
        if (cacheCalloutView == null) {
            cacheCalloutView = CalloutHelper.createCalloutView(mapView.context, markerModel.callout!!)
        }
        return cacheCalloutView
    }

    private fun releaseCalloutView(){
        cacheCalloutView = null
    }

    private fun rotateAndTranslateAnimation(marker: Marker, rotate: Float, duration: Long, dest: LatLng, moveWithRotate: Boolean) {
        if (moveWithRotate) {
            val rotateAnimator = ObjectAnimator.ofFloat(marker, "rotation", marker.rotation, rotate)
            rotateAnimator.setDuration(duration)
            rotateAnimator.interpolator = LinearInterpolator()
            val translateAnimator = ObjectAnimator.ofObject(marker, "position", object : TypeEvaluator<LatLng?> {
                override fun evaluate(fraction: Float, latLngStart: LatLng?, latLngEnd: LatLng?): LatLng? {
                    if (latLngStart == null || latLngEnd == null) return marker.position
                    val curLatitude = latLngStart.latitude + fraction * (latLngEnd.latitude - latLngStart.latitude)
                    val curLongitude = latLngStart.longitude + fraction * (latLngEnd.longitude - latLngStart.longitude)
                    return LatLng(curLatitude, curLongitude)
                }
            }, marker.position, dest)
            translateAnimator.setDuration(duration)
            translateAnimator.interpolator = LinearInterpolator()

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(rotateAnimator, translateAnimator)
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })
            animatorSet.start()
        } else {
            val rotateDuration = (duration * 0.3).toLong()
            val translateDuration = duration - rotateDuration
            val rotateAnimator = ObjectAnimator.ofFloat(marker, "rotation", marker.rotation, rotate)
            rotateAnimator.setDuration(translateDuration)
            rotateAnimator.interpolator = LinearInterpolator()
            rotateAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    translateAnimation(dest, translateDuration, marker)
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })
            rotateAnimator.start()
        }
    }

    private fun translateAnimation(dest: LatLng, duration: Long, marker: Marker) {
        val translateAnimator: ObjectAnimator = ObjectAnimator.ofObject(marker, "position", object : TypeEvaluator<LatLng> {
            override fun evaluate(fraction: Float, latLngStart: LatLng?, latLngEnd: LatLng?): LatLng? {
                if (latLngStart == null || latLngEnd == null) return marker.position
                val curLatitude = latLngStart.latitude + fraction * (latLngEnd.latitude - latLngStart.latitude)
                val curLongitude = latLngStart.longitude + fraction * (latLngEnd.longitude - latLngStart.longitude)
                return LatLng(curLatitude, curLongitude)
            }
        }, marker.position, dest)
        translateAnimator.setDuration(duration)
        translateAnimator.interpolator = LinearInterpolator()
        translateAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }

        })
        translateAnimator.start()
    }


    var mTranslateAnimator: MarkerTranslateAnimator? = null
    fun moveAlong(points: List<uts.sdk.modules.uniMapTencent.latlng.LatLng>, duration: Double?, callback: ICallBack) {
        if (mTranslateAnimator != null) {
            mTranslateAnimator?.cancelAnimation()
        }

        val path = points.map {
            uts.sdk.modules.uniMapTencent.latlng.LatLng.convertLatLng(it)
        }
        mTranslateAnimator = MarkerTranslateAnimator(
            mMarker,
            duration?.toLong() ?: 1000,
            path.toTypedArray(),
            true
        )
        mTranslateAnimator?.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mTranslateAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                mTranslateAnimator = null
            }
        })
        mTranslateAnimator?.startAnimation()
        callback.callback(
            mapOf(
                "type" to "success",
                "errMsg" to ""
            )
        )
    }


    private var errorBitmap: Bitmap? = null
    private fun createBitmapDescriptor(iconPath: String, width: Int?, height: Int?, completeCallback: ICallBack) {
        val requestBuilder = Glide.with(mapView.context)
            .asBitmap()
            .load(iconPath)
        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                completeCallback.callback(BitmapDescriptorFactory.fromBitmap(resource))
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                completeCallback.callback(null)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                val drawableId = mapView.context.resources.getIdentifier("uni_app_map_marker_ic", "drawable", mapView.context.packageName)
                errorBitmap = getBitmapFromDrawable(drawableId, width, height)
                completeCallback.callback(BitmapDescriptorFactory.fromBitmap(errorBitmap))
            }
        }

        if (width == null || height == null) {
            requestBuilder.into(target)
        } else {
            requestBuilder.override(width, height)
                .into(target)
        }
    }

    private fun getBitmapFromDrawable(@DrawableRes drawableRes: Int, width: Int?, height: Int?): Bitmap {
        val drawable: Drawable = mapView.context.resources.getDrawable(drawableRes)
        val bitmap = Bitmap.createBitmap(width ?: drawable.intrinsicWidth, height ?: drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width ?: canvas.width, height ?: canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun getRealMarker(): Marker? {
        return mMarker
    }

    fun isAlwaysDisPlay(): Boolean {
        return markerModel.callout?.display == true
    }

    fun hasCallout(): Boolean {
        return markerModel.callout != null
    }

    override fun destroy() {
        mMarker?.remove()
        mMarker?.releaseData()
        mMarker = null
        errorBitmap?.recycle()
        errorBitmap = null
        releaseCalloutView()
    }


}