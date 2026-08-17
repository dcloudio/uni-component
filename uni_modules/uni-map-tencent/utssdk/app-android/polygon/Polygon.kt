package uts.sdk.modules.uniMapTencent.polygon

import uts.sdk.modules.uniMapTencent.latlng.LatLng

class Polygon(var points: List<LatLng>)  {
    var strokeWidth: Double? = null
    var strokeColor: Int? = null
    var fillColor: Int? = null
    var zIndex: Double? = null
}