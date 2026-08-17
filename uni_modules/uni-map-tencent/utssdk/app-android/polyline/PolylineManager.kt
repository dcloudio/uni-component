package uts.sdk.modules.uniMapTencent.polyline

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions.LineType


class PolylineManager(val context: Context, val map: TencentMap) {
    private var mPolylineCaches = mutableListOf<com.tencent.tencentmap.mapsdk.maps.model.Polyline>()

    fun setPolyline(list: List<Polyline>?) {
        clearPolyline()
        if (list != null) {
            for (polyline in list) {
                mPolylineCaches.add(createAndAttachPolyline(polyline))
            }
        }
    }

    private fun createAndAttachPolyline(polyline: Polyline): com.tencent.tencentmap.mapsdk.maps.model.Polyline {
        val latlngs: List<LatLng> = polyline.points.map { uts.sdk.modules.uniMapTencent.latlng.LatLng.convertLatLng(it) }
        val polylineOptions = PolylineOptions()
        polylineOptions.addAll(latlngs)

        if (polyline.width != null) {
            polylineOptions.width(polyline.width!!.toFloat())
        }

        if (polyline.colorList != null) {
            polylineOptions.lineType(LineType.LINE_TYPE_MULTICOLORLINE)
            polylineOptions.gradient(true)
            val indexes = mutableListOf<Int>()
            for (i in 0 until polyline.colorList!!.size) {
                indexes.add(i)
            }
            polylineOptions.colors(polyline.colorList!!.toIntArray(), indexes.toIntArray())
        } else {
            if (polyline.color != null) {
                polylineOptions.color(polyline.color!!)
            }
        }

        if (polyline.dottedLine) {
            polylineOptions.pattern(mutableListOf(35, 20))
        } else {
            polylineOptions.pattern(null)
        }

        if (polyline.arrowLine) {
            //虚线和箭头线冲突，箭头线优先。
            polylineOptions.pattern(null)
            polylineOptions.colors(intArrayOf(), intArrayOf())

            if (polyline.arrowIconPath != null) {
                val bitmapDescriptor: BitmapDescriptor = if (polyline.arrowIconPath!!.startsWith("file:///android_asset/")) {
                    BitmapDescriptorFactory.fromAsset(polyline.arrowIconPath)
                } else {
                    BitmapDescriptorFactory.fromPath(polyline.arrowIconPath)
                }
                polylineOptions.arrowTexture(bitmapDescriptor)
            }
            polylineOptions.arrowSpacing((30 * getScreenDensity(context)).toInt())
            polylineOptions.arrow(true)
        } else {
            polylineOptions.arrowTexture(null)
            polylineOptions.arrow(false)
        }

        return map.addPolyline(polylineOptions)
    }

    private fun clearPolyline() {
        for (polyline in mPolylineCaches) {
            polyline.remove()
        }
        mPolylineCaches.clear()
    }

    fun destroy(){
        clearPolyline()
    }

    private fun getScreenDensity(context: Context): Float {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.density
    }
}