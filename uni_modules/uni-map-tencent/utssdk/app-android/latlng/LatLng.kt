package uts.sdk.modules.uniMapTencent.latlng

import com.tencent.tencentmap.mapsdk.maps.model.LatLng

class LatLng {

    companion object {
        fun convertLatLng(latLng: uts.sdk.modules.uniMapTencent.latlng.LatLng): LatLng {
            return LatLng(latLng.latitude, latLng.longitude)
        }
    }

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    override fun toString(): String {
        return "LatLng(latitude=$latitude, longitude=$longitude)"
    }
}