package uts.sdk.modules.uniMapTencent.ground

import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds
import uts.sdk.modules.uniMapTencent.latlng.LatLng


class Bounds(val southwest: LatLng, val northeast: LatLng) {

    fun convertBounds(): LatLngBounds {
        return LatLngBounds(LatLng.convertLatLng(northeast), LatLng.convertLatLng(southwest))
    }

}

class Ground(val id: String, val src: String, val bounds: Bounds) {

    var visible: Boolean = true
    var zIndex: Double = 0.0
    var opacity: Double = 1.0
        set(value) {
            field = if (value >= 1) {
                1.0
            } else if (value <= 0) {
                0.0
            } else {
                value
            }
        }

}