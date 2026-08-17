package uts.sdk.modules.uniMapTencent.marker

import com.tencent.tencentmap.mapsdk.maps.model.Marker

interface IUniMarker {
    fun getRealMarker(): Marker?
    fun destroy()
}