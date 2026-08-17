package uts.sdk.modules.uniMapTencent.circle

import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.model.CircleOptions
import com.tencent.tencentmap.mapsdk.maps.model.LatLng

class CircleManager(val map: TencentMap) {
    private var mCircleCaches = mutableListOf<com.tencent.tencentmap.mapsdk.maps.model.Circle>()

    fun setCircle(list: List<Circle>?) {
        clearCircle()
        if (list != null) {
            for (circle in list) {
                mCircleCaches.add(createAndAttachCircle(circle))
            }
        }
    }

    private fun createAndAttachCircle(circle: Circle): com.tencent.tencentmap.mapsdk.maps.model.Circle {
        val circleOptions = CircleOptions()
        circleOptions.center(LatLng(circle.latitude, circle.longitude))
        circleOptions.radius(circle.radius)

        if (circle.color != null) {
            circleOptions.strokeColor(circle.color!!)
        }

        if (circle.fillColor != null) {
            circleOptions.fillColor(circle.fillColor!!)
        }

        if (circle.strokeWidth != null) {
            circleOptions.strokeWidth(circle.strokeWidth!!.toFloat())
        }

        return map.addCircle(circleOptions)
    }

    private fun clearCircle() {
        for (circle in mCircleCaches) {
            circle.remove()
        }
        mCircleCaches.clear()
    }

    fun destroy(){
        clearCircle()
    }

}