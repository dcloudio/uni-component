package uts.sdk.modules.uniMapTencent.marker

import android.view.View
import com.tencent.tencentmap.mapsdk.maps.MapView
import com.tencent.tencentmap.mapsdk.maps.model.Marker
import uts.sdk.modules.uniMapTencent.ICallBack
import uts.sdk.modules.uniMapTencent.latlng.LatLng
import java.util.concurrent.ConcurrentHashMap

// TODO: 暂时不做customcallout、label、点聚合
class MarkerManager(val mapView: MapView) {
    private var mMarkerCaches = ConcurrentHashMap<Int, TencentMarker>()

    fun setMarkers(list: List<MarkerModel>) {
        clearMarkers()
        addMarkers(list)
    }

    fun addMarkers(list: List<MarkerModel>) {
        for (markerModel in list) {
            if (mMarkerCaches.containsKey(markerModel.id)) {
                val tencentMarker = mMarkerCaches[markerModel.id]
                tencentMarker?.updateMarkerOptions(markerModel)
                continue
            }
            mMarkerCaches[markerModel.id] = TencentMarker(markerModel, mapView)
        }
    }

    fun removeMarkers(ids: List<Int>) {
        for (id in ids) {
            val marker = mMarkerCaches.remove(id)
            marker?.destroy()
        }
    }


    fun translateMarker(markerId: Int, destination: LatLng, rotate: Double?, moveWithRotate: Boolean?, duration: Double?, callback: ICallBack) {
        val tencentMarker = mMarkerCaches[markerId]
        if (tencentMarker == null) {
            callback.callback(
                mapOf(
                    "type" to "fail",
                    "errMsg" to "marker not found"
                )
            )
            return
        }
        tencentMarker.translateMarker(destination, rotate, moveWithRotate, duration, callback)
    }


    fun moveAlongMarker(markerId: Int, path: List<LatLng>, duration: Double, callback: ICallBack) {
        val tencentMarker = mMarkerCaches[markerId]
        if (tencentMarker == null) {
            callback.callback(
                mapOf(
                    "type" to "fail",
                    "errMsg" to "marker not found"
                )
            )
            return
        }
        tencentMarker.moveAlong(path, duration, callback)
    }

    fun clearMarkers() {
        mMarkerCaches.forEach { (_, u) ->
            u.destroy()
        }
        mMarkerCaches.clear()
    }

    fun getCalloutView(marker: Marker?): View? {
        if (marker == null) return null
        val tencentMarker = getTencentMarker(marker)
        return tencentMarker?.getCalloutView()
    }

    fun hideAllMarkerCallout(excludeMarker: Marker? = null) {
        mMarkerCaches.forEach { (_, u) ->
            u.getRealMarker()?.let {
                if (it !== excludeMarker && it.isInfoWindowShown) {
                    if (!u.hasCallout() || (u.hasCallout() && !u.isAlwaysDisPlay())) {
                        it.hideInfoWindow()
                    }
                }
            }
        }
    }

    fun showOrHideMarkerCallout(marker: Marker) {
        val tencentMarker = getTencentMarker(marker) ?: return
        if(tencentMarker.hasCallout() && tencentMarker.isAlwaysDisPlay()) {
           return
        }
        if (marker.isInfoWindowShown) {
            marker.hideInfoWindow()
        } else {
            marker.showInfoWindow()
        }
    }

    fun getTencentMarker(marker: Marker): TencentMarker? {
        mMarkerCaches.forEach { (_, u) ->
            if (u.getRealMarker() === marker) {
                return u
            }
        }
        return null
    }

    fun destroy() {
        clearMarkers()
    }
}


