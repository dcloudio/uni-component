package uts.sdk.modules.uniMapTencent.polyline

import uts.sdk.modules.uniMapTencent.latlng.LatLng


class Polyline(var points: List<LatLng>) {
    var color : Int? = null
    var width: Double? = null
    var dottedLine = false
    var arrowLine = false
    var arrowIconPath: String? = null
    var colorList : List<Int>? = null
}