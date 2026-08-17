package uts.sdk.modules.uniMapTencent.control

import com.tencent.tencentmap.mapsdk.maps.MapView
import uts.sdk.modules.uniMapTencent.ICallBack


class ControlManager(private val mapView: MapView) {
    private var mControlCaches = mutableListOf<ControlView>()

    fun setControl(list: List<Control>?, callback: ICallBack) {
        clearControl()
        if (list != null) {
            for (control in list) {
                val controlView = ControlView(control, mapView, callback)
                controlView.attachView()
                mControlCaches.add(controlView)
            }
        }
    }

    private fun clearControl() {
        mControlCaches.forEach {
            it.detachView()
        }
        mControlCaches.clear()
    }

    fun destroy(){
        clearControl()
    }
}