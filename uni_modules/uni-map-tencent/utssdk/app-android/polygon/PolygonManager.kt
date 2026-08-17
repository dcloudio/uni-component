package uts.sdk.modules.uniMapTencent.polygon

import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.PolygonOptions

class PolygonManager(val map: TencentMap) {
    private var mPolygonCaches = mutableListOf<com.tencent.tencentmap.mapsdk.maps.model.Polygon>()

    fun setPolygon(list: List<Polygon>?) {
        clearPolygon()
        if (list != null) {
            for (polygon in list) {
                mPolygonCaches.add(createAndAttachPolygon(polygon))
            }
        }
    }

    private fun createAndAttachPolygon(polygon: Polygon): com.tencent.tencentmap.mapsdk.maps.model.Polygon {
        val latlngs: List<LatLng> = polygon.points.map { uts.sdk.modules.uniMapTencent.latlng.LatLng.convertLatLng(it) }
        val polygonOptions = PolygonOptions()
        polygonOptions.addAll(latlngs)

        if (polygon.strokeWidth != null) {
            polygonOptions.strokeWidth(polygon.strokeWidth!!.toFloat())
        }

        if (polygon.strokeColor != null) {
            polygonOptions.strokeColor(polygon.strokeColor!!)
        }

        if (polygon.fillColor != null) {
            polygonOptions.fillColor(polygon.fillColor!!)
        }

        if (polygon.zIndex != null) {
            polygonOptions.zIndex(polygon.zIndex!!.toInt())
        }

        return map.addPolygon(polygonOptions)
    }


    private fun clearPolygon() {
        for (polygon in mPolygonCaches) {
            polygon.remove()
        }
        mPolygonCaches.clear()
    }

    fun destroy(){
        clearPolygon()
    }
}