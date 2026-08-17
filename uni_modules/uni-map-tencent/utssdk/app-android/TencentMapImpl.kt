package uts.sdk.modules.uniMapTencent

import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.LocationSource
import com.tencent.tencentmap.mapsdk.maps.MapView
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.TencentMap.OnMyLocationClickListener
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds
import com.tencent.tencentmap.mapsdk.maps.model.MapViewType
import com.tencent.tencentmap.mapsdk.maps.model.Marker
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER
import com.tencent.tencentmap.mapsdk.maps.model.TencentMapGestureListener
import uts.sdk.modules.uniMapTencent.circle.Circle
import uts.sdk.modules.uniMapTencent.circle.CircleManager
import uts.sdk.modules.uniMapTencent.control.Control
import uts.sdk.modules.uniMapTencent.control.ControlManager
import uts.sdk.modules.uniMapTencent.ground.Bounds
import uts.sdk.modules.uniMapTencent.ground.Ground
import uts.sdk.modules.uniMapTencent.ground.GroundManager
import uts.sdk.modules.uniMapTencent.marker.MarkerManager
import uts.sdk.modules.uniMapTencent.marker.MarkerModel
import uts.sdk.modules.uniMapTencent.polygon.Polygon
import uts.sdk.modules.uniMapTencent.polygon.PolygonManager
import uts.sdk.modules.uniMapTencent.polyline.Polyline
import uts.sdk.modules.uniMapTencent.polyline.PolylineManager


class TencentMapImpl : IInternalMap, LocationSource, TencentLocationListener {
    private var mapView: MapView? = null
    private var mapTouchHostLayout: MapTouchHostLayout? = null
    private var mDragged = false
    private var mCameraType = "drag"
    private var isSetUpdate = false
    private var mZoomLevel = 16f
    private var isChangeStart = false
    private var isRegisterGestureListener = false
    private var isSetup = false
    private var polylineManager: PolylineManager? = null
    private var polygonManager: PolygonManager? = null
    private var circleManager: CircleManager? = null
    private var controlManager: ControlManager? = null
    private var groundManager: GroundManager? = null
    private var markerManager: MarkerManager? = null

    private var locationManager: TencentLocationManager? = null
    private var locationRequest: TencentLocationRequest? = null

    /**
     * 只有定位成功，才能注册事件
     */
    private var canRegisterMyLocationClick = false

    /**
     * 一定要持有，OnMyLocationClickListener在腾讯地图内部是弱引用，不持有会被回收
     */
    private var myLocationClickListener: OnMyLocationClickListener? = null

    /**
     * 当前位置坐标
     */
    private val currentLocationLatLng = uts.sdk.modules.uniMapTencent.latlng.LatLng(0.0, 0.0)

    private var infoWindowAdapter: TencentMap.InfoWindowAdapter? = null

    override fun getMap(context: Context): View {
        val options = TencentMapOptions()
        options.setMapViewType(MapViewType.TextureView)
//        options.mapKey = ""
        val createdMapView = MapView(context, options)
        mapView = createdMapView
        mapTouchHostLayout = MapTouchHostLayout(context).also { host ->
            host.addView(
                createdMapView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
        setup(context)
        defaultConfig()
        return mapTouchHostLayout as MapTouchHostLayout
    }

    private fun setup(context: Context) {
        if (isSetup && mapView != null) {
            return
        }
        polylineManager = PolylineManager(context, mapView?.map!!)
        polygonManager = PolygonManager(mapView?.map!!)
        circleManager = CircleManager(mapView?.map!!)
        controlManager = ControlManager(mapView!!)
        groundManager = GroundManager(context, mapView?.map!!)
        markerManager = MarkerManager(mapView!!)
        isSetup = true
        locationManager = TencentLocationManager.getInstance(context)
        locationRequest = TencentLocationRequest.create()
        locationRequest?.interval = 3000
        mapView?.map?.setLocationSource(this)
    }

    private fun defaultConfig() {
        if (isRegisterGestureListener) {
            return
        }

        mapView?.map?.enableMultipleInfowindow(true)
        infoWindowAdapter = object : TencentMap.InfoWindowAdapter {
            override fun getInfoWindow(p0: Marker?): View? {
                return markerManager?.getCalloutView(p0)
            }

            override fun getInfoContents(p0: Marker?): View? {
                return markerManager?.getCalloutView(p0)
            }
        }

        mapView?.map?.setInfoWindowAdapter(infoWindowAdapter!!)

        mapView?.map?.addTencentMapGestureListener(object : TencentMapGestureListener {
            override fun onDoubleTap(p0: Float, p1: Float): Boolean {
                return false
            }

            override fun onSingleTap(p0: Float, p1: Float): Boolean {
                return false
            }

            override fun onFling(p0: Float, p1: Float): Boolean {
                mDragged = true
                isSetUpdate = false
                return false
            }

            override fun onScroll(p0: Float, p1: Float): Boolean {
                return false
            }

            override fun onLongPress(p0: Float, p1: Float): Boolean {
                return false
            }

            override fun onDown(p0: Float, p1: Float): Boolean {
                return false
            }

            override fun onUp(p0: Float, p1: Float): Boolean {
                mDragged = false
                return false
            }

            override fun onTwoFingerMoveAgainst(p0: TencentMapGestureListener.TwoFingerMoveAgainstStatus?, p1: CameraPosition?): Boolean {
                return true
            }

            override fun onMapStable() {
            }

//            override fun onMapStableBy(p0: CameraPosition.Trigger?) {
//            }
        })

        mapView?.map?.setOnMapClickListener {
            markerManager?.hideAllMarkerCallout()
            if (mapClickCallBack == null) {
                return@setOnMapClickListener
            }
            val point = mapView?.map?.projection?.toScreenLocation(it)
            var screenX: Int? = null
            var screenY: Int? = null
            if (point != null) {
                mapView?.let { mapView ->
                    val mapViewLocation = IntArray(2)
                    mapView.getLocationOnScreen(mapViewLocation)
                    screenX = point.x + mapViewLocation[0]
                    screenY = point.y + mapViewLocation[1]
                }
            }

            var dx = 0f
            var dy = 0f
            mapView?.let { mapView ->
                var temp: View? = mapView
                while (temp != null) {
                    (temp.parent as? View)?.let { p ->
                        dx += p.scrollX.toFloat()
                        dy += p.scrollY.toFloat()
                    }
                    temp = temp.parent as? View
                }
            }
            mapClickCallBack?.callback(
                mapOf(
                    "type" to "tap",
                    "detail" to mapOf(
                        "latitude" to it.latitude,
                        "longitude" to it.longitude,
                        "x" to px2dp(mapView!!.context, (point?.x?.toFloat() ?: 0f)),
                        "y" to px2dp(mapView!!.context, (point?.y?.toFloat() ?: 0f)),
                        "screenX" to px2dp(mapView!!.context, (screenX?.toFloat() ?: 0f)) ,
                        "screenY" to px2dp(mapView!!.context, (screenY?.toFloat() ?: 0f)) ,
                        "dx" to px2dp(mapView!!.context, dx),
                        "dy" to px2dp(mapView!!.context, dy)
                    )
                )
            )
        }

        mapView?.map?.setOnMarkerClickListener { marker ->
            marker.setOnTop()
            markerManager?.hideAllMarkerCallout(marker)
            markerManager?.showOrHideMarkerCallout(marker)
            if (markerClickCallBack == null) {
                return@setOnMarkerClickListener true
            }
            val point = mapView?.map?.projection?.toScreenLocation(marker.position)
            var screenX: Int? = null
            var screenY: Int? = null
            if (point != null) {
                mapView?.let {
                    val mapViewLocation = IntArray(2)
                    it.getLocationOnScreen(mapViewLocation)
                    screenX = point.x + mapViewLocation[0]
                    screenY = point.y + mapViewLocation[1]
                }
            }

            var dx = 0f
            var dy = 0f
            mapView?.let {
                var temp: View? = it
                while (temp != null) {
                    (temp!!.parent as? View)?.let { p ->
                        dx += p.scrollX.toFloat()
                        dy += p.scrollY.toFloat()
                    }
                    temp = temp!!.parent as? View
                }
            }
            val tencentMarker = markerManager?.getTencentMarker(marker)
            tencentMarker?.let {
                markerClickCallBack?.callback(
                    mapOf(
                        "type" to "markertap",
                        "detail" to mapOf(
                            "markerId" to it.markerModel.id,
                            "x" to px2dp(mapView!!.context, (point?.x?.toFloat() ?: 0f)),
                            "y" to px2dp(mapView!!.context, (point?.y?.toFloat() ?: 0f)),
                            "screenX" to px2dp(mapView!!.context, (screenX?.toFloat() ?: 0f)) ,
                            "screenY" to px2dp(mapView!!.context, (screenY?.toFloat() ?: 0f)) ,
                            "dx" to px2dp(mapView!!.context, dx),
                            "dy" to px2dp(mapView!!.context, dy)
                        )
                    )
                )
            }
            true
        }

        mapView?.map?.setOnInfoWindowClickListener(object : TencentMap.OnInfoWindowClickListener{
            override fun onInfoWindowClick(marker: Marker) {
                if (calloutClickCallBack == null) {
                    return
                }
                val tencentMarker = markerManager?.getTencentMarker(marker)
                tencentMarker?.let {
                    calloutClickCallBack?.callback(
                        mapOf(
                            "type" to "callouttap",
                            "detail" to mapOf(
                                "markerId" to it.markerModel.id
                            )
                        )
                    )
                }
            }

            override fun onInfoWindowClickLocation(p0: Int, p1: Int, p2: Int, p3: Int) {
            }
        })

        isRegisterGestureListener = true
    }

    override fun onStart() {
        mapView?.onStart()
    }

    override fun onResume() {
        mapView?.onResume()
    }

    override fun onPause() {
        mapView?.onPause()
    }

    override fun onStop() {
        mapView?.onStop()
    }

    override fun onDestroy() {
        mapTouchHostLayout?.removeAllViews()
        mapView?.onDestroy()

        locationRegisterHandler.removeCallbacksAndMessages(null)
        mapView?.map?.setLocationSource(null)
        deactivate()
        mapView = null
        mapTouchHostLayout = null
        polylineManager?.destroy()
        polygonManager?.destroy()
        circleManager?.destroy()
        controlManager?.destroy()
        groundManager?.destroy()
        markerManager?.destroy()
        isSetup = false
    }

    override fun setCenter(latitude: Double, longitude: Double, animate: Boolean) {
        isSetUpdate = true
        if (animate){
            mapView?.map?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
        }else{
            mapView?.map?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
        }
    }

    override fun setMinScale(zoom: Int) {
        mapView?.map?.setMinZoomLevel(zoom)
    }

    override fun setMaxScale(zoom: Int) {

        mapView?.map?.setMaxZoomLevel(zoom)
    }

    override fun setScale(zoom: Int, animate: Boolean) {
        mZoomLevel = zoom.toFloat()
        isSetUpdate = true
        if (animate){
            mapView?.map?.animateCamera(CameraUpdateFactory.zoomTo(zoom.toFloat()))
        } else {
            mapView?.map?.moveCamera(CameraUpdateFactory.zoomTo(zoom.toFloat()))
        }
    }

    override fun getScale(): Int {
        val cameraPosition = mapView?.map?.cameraPosition
        return cameraPosition?.zoom?.toInt() ?: mZoomLevel.toInt()
    }

    override fun setTheme(theme: String) {
        mapView?.map?.mapType = when (theme) {
            "satellite" -> TencentMap.MAP_TYPE_SATELLITE
            else -> TencentMap.MAP_TYPE_NORMAL
        }
    }

    override fun setLayerStyle(id: String) {
        mapView?.map?.mapType = id.toInt()
    }

    override fun addMarkers(markerModels: List<MarkerModel>) {
        markerManager?.addMarkers(markerModels)
    }

    override fun setMarkers(markerModels: List<MarkerModel>) {
        markerManager?.setMarkers(markerModels)
    }

    override fun removeMarkers(ids: List<Int>) {
        markerManager?.removeMarkers(ids)
    }

    override fun setPolyline(list: List<Polyline>) {
        polylineManager?.setPolyline(list)
    }

    override fun setCircle(list: List<Circle>) {
        circleManager?.setCircle(list)
    }

    override fun setControl(list: List<Control>) {
        controlManager?.setControl(list, object : ICallBack {
            override fun callback(param: Any?) {
                controlTapCallBack?.callback(param)
            }
        })
    }

    override fun setPolygon(list: List<Polygon>) {
        polygonManager?.setPolygon(list)
    }

    override fun addGroundOverlay(ground: Ground, iCallBack: ICallBack) {
        groundManager?.addGroundOverlay(ground, iCallBack)
    }

    override fun updateGroundOverlay(ground: Ground, iCallBack: ICallBack) {
        groundManager?.updateGroundOverlay(ground, iCallBack)
    }

    override fun removeGroundOverlay(id: String, iCallBack: ICallBack) {
        groundManager?.removeGroundOverlay(id, iCallBack)
    }

    override fun setIncludePoints(points: List<uts.sdk.modules.uniMapTencent.latlng.LatLng>, animate: Boolean) {
        if (points.isEmpty()){
            return
        }
        isSetUpdate = true
        LatLngBounds.Builder().apply {
            points.forEach {
                include(uts.sdk.modules.uniMapTencent.latlng.LatLng.convertLatLng(it))
            }
            if (animate){
                mapView?.map?.animateCamera(CameraUpdateFactory.newLatLngBounds(build(), 0))
            }else{
                mapView?.map?.moveCamera(CameraUpdateFactory.newLatLngBounds(build(), 0))
            }
        }
    }

    override fun enable3D(enable: Boolean) {
        mapView?.map?.setBuilding3dEffectEnable(enable)
    }

    override fun showCompass(enable: Boolean) {
        mapView?.map?.uiSettings?.isCompassEnabled = enable
    }

    override fun enableZoom(enable: Boolean) {
        mapView?.map?.uiSettings?.isZoomGesturesEnabled = enable
    }

    override fun enableScroll(enable: Boolean) {
        mapView?.map?.uiSettings?.isScrollGesturesEnabled = enable
    }

    override fun enableRotate(enable: Boolean) {
        mapView?.map?.uiSettings?.isRotateGesturesEnabled = enable
    }

    override fun setRotate(rotate: Float, animate: Boolean) {
        isSetUpdate = true
        val cameraPosition = mapView?.map?.cameraPosition
        if (animate){
            mapView?.map?.animateCamera(cameraPosition?.tilt?.let { CameraUpdateFactory.rotateTo(rotate, it) })
        }else{
            mapView?.map?.moveCamera(cameraPosition?.tilt?.let { CameraUpdateFactory.rotateTo(rotate, it) })
        }
    }

    override fun setSkew(skew: Float, animate: Boolean) {
        isSetUpdate = true
        val cameraPosition = mapView?.map?.cameraPosition
        if (animate){
            mapView?.map?.animateCamera(cameraPosition?.bearing?.let { CameraUpdateFactory.rotateTo(it, skew) })
        }else{
            mapView?.map?.moveCamera(cameraPosition?.bearing?.let { CameraUpdateFactory.rotateTo(it, skew) })
        }
    }

    override fun setEnableOverlooking(enable: Boolean) {
        mapView?.map?.uiSettings?.isTiltGesturesEnabled = enable
    }

    override fun setEnableSatellite(enable: Boolean) {
        mapView?.map?.mapType = if (enable) TencentMap.MAP_TYPE_SATELLITE else TencentMap.MAP_TYPE_NORMAL
    }

    override fun setEnableTraffic(enable: Boolean) {
        mapView?.map?.isTrafficEnabled = enable
    }

    override fun setEnablePoi(enable: Boolean) {
        mapView?.map?.setPoisEnabled(enable)
    }

    override fun setEnableBuilding(enable: Boolean) {
        enable3D(enable)
    }

    override fun setShowLocation(show: Boolean) {
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationType(LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
        mapView?.map?.setMyLocationStyle(myLocationStyle)
        mapView?.map?.isMyLocationEnabled = show

        if (show) {
            registerMyLocationClickListener(0)
        }
    }

    private var locationRegisterHandler = Handler(Looper.getMainLooper())
    private  fun registerMyLocationClickListener(count: Int){
        if (count > 10){
            return
        }
        if (canRegisterMyLocationClick) {
            myLocationClickListener = OnMyLocationClickListener { p0 ->
                if (p0 != null) {
                    val point = mapView?.map?.projection?.toScreenLocation(p0)
                    var screenX: Int? = null
                    var screenY: Int? = null
                    if (point != null) {
                        mapView?.let {
                            val mapViewLocation = IntArray(2)
                            it.getLocationOnScreen(mapViewLocation)
                            screenX = point.x + mapViewLocation[0]
                            screenY = point.y + mapViewLocation[1]
                        }
                    }

                    var dx = 0f
                    var dy = 0f
                    mapView?.let {
                        var temp: View? = it
                        while (temp != null) {
                            (temp.parent as? View)?.let { p ->
                                dx += p.scrollX.toFloat()
                                dy += p.scrollY.toFloat()
                            }
                            temp = temp.parent as? View
                        }
                    }

                    myLocationClickCallBack?.callback(
                        mapOf(
                            "type" to "anchorpointtap",
                            "detail" to mapOf(
                                "latitude" to p0.latitude,
                                "longitude" to p0.longitude,
                                "x" to px2dp(mapView!!.context, (point?.x?.toFloat() ?: 0f)),
                                "y" to px2dp(mapView!!.context, (point?.y?.toFloat() ?: 0f)),
                                "screenX" to px2dp(mapView!!.context, (screenX?.toFloat() ?: 0f)) ,
                                "screenY" to px2dp(mapView!!.context, (screenY?.toFloat() ?: 0f)) ,
                                "dx" to px2dp(mapView!!.context, dx),
                                "dy" to px2dp(mapView!!.context, dy)
                            )
                        )
                    )
                }
                true
            }
            mapView?.map?.setMyLocationClickListener(myLocationClickListener)
        }else{
            locationRegisterHandler.postDelayed({
                registerMyLocationClickListener(count + 1)
            }, 3000)
        }
    }

    override fun setEnableIndoorMap(enable: Boolean) {
        mapView?.map?.setIndoorEnabled(enable)
    }

    override fun setOnMapLoadedCallback(iCallBack: ICallBack) {
        mapView?.map?.addOnMapLoadedCallback {
            iCallBack.callback(
                mapOf(
                    "type" to "updated",
                    "detail" to emptyMap<String, Any>()
                )
            )
        }
    }

    private var mapClickCallBack: ICallBack? = null
    override fun setOnMapClickCallback(iCallBack: ICallBack) {
        mapClickCallBack = iCallBack
    }

    override fun setOnMapRegionChange(iCallBack: ICallBack) {
        mapView?.map?.setOnCameraChangeListener(object : TencentMap.OnCameraChangeListener {
            override fun onCameraChange(p0: CameraPosition?) {
                if (!isChangeStart) {
                    mCameraType = if (mDragged) {
                        "gesture"
                    } else {
                        "update"
                    }

                    mZoomLevel = p0?.zoom!!
                    iCallBack.callback(
                        mapOf(
                            "type" to "begin",
                            "detail" to mapOf<String, Any>()
                        )
                    )
                    isChangeStart = true
                }
            }

            override fun onCameraChangeFinished(p0: CameraPosition?) {
//                p0?.target?.let {
//                    mMapCenterPoint.latitude = it.latitude
//                    mMapCenterPoint.longitude = it.longitude
//                }
                if (isChangeStart) {
                    isChangeStart = false
                    mCameraType = if (isSetUpdate) {
                        "update"
                    } else if (p0?.zoom != mZoomLevel) {
                        "scale"
                    } else {
                        "drag"
                    }
                    iCallBack.callback(
                        mapOf(
                            "type" to "end",
                            "causedBy" to mCameraType,
                            "detail" to mapOf(
                                "skew" to (p0?.tilt ?: 0),
                                "rotate" to (p0?.bearing ?: 0)
                            )
                        )
                    )
                    isSetUpdate = false
                }
            }
        })
    }


    private var controlTapCallBack: ICallBack? = null
    override fun setControlTap(iCallBack: ICallBack) {
        controlTapCallBack = iCallBack
    }

    private var myLocationClickCallBack: ICallBack? = null
    override fun setMyLocationClick(iCallBack: ICallBack) {
        myLocationClickCallBack = iCallBack
    }

    private var markerClickCallBack: ICallBack? = null
    override fun setOnMarkerClick(iCallBack: ICallBack) {
        markerClickCallBack = iCallBack
    }

    private var calloutClickCallBack: ICallBack? = null
    override fun setOnCalloutClick(iCallBack: ICallBack) {
        calloutClickCallBack = iCallBack
    }

    override fun setPoiTapClick(iCallBack: ICallBack) {
        mapView?.map?.setOnMapPoiClickListener {
            val point = mapView?.map?.projection?.toScreenLocation(it.position)
            var screenX: Int? = null
            var screenY: Int? = null
            if (point != null) {
                mapView?.let { mapView ->
                    val mapViewLocation = IntArray(2)
                    mapView.getLocationOnScreen(mapViewLocation)
                    screenX = point.x + mapViewLocation[0]
                    screenY = point.y + mapViewLocation[1]
                }
            }

            var dx = 0f
            var dy = 0f
            mapView?.let { mapView ->
                var temp: View? = mapView
                while (temp != null) {
                    (temp.parent as? View)?.let { p ->
                        dx += p.scrollX.toFloat()
                        dy += p.scrollY.toFloat()
                    }
                    temp = temp.parent as? View
                }
            }
            iCallBack.callback(
                mapOf(
                    "type" to "poitap",
                    "detail" to mapOf(
                        "latitude" to it.position.latitude,
                        "longitude" to it.position.longitude,
                        "name" to it.name,
                        "x" to px2dp(mapView!!.context, (point?.x?.toFloat() ?: 0f)),
                        "y" to px2dp(mapView!!.context, (point?.y?.toFloat() ?: 0f)),
                        "screenX" to px2dp(mapView!!.context, (screenX?.toFloat() ?: 0f)) ,
                        "screenY" to px2dp(mapView!!.context, (screenY?.toFloat() ?: 0f)) ,
                        "dx" to px2dp(mapView!!.context, dx),
                        "dy" to px2dp(mapView!!.context, dy)
                    )
                )
            )
        }
    }

    override fun getCenterLocation(): uts.sdk.modules.uniMapTencent.latlng.LatLng {
        mapView?.map?.cameraPosition?.target?.let {
            return uts.sdk.modules.uniMapTencent.latlng.LatLng(it.latitude, it.longitude)
        }
        return uts.sdk.modules.uniMapTencent.latlng.LatLng(0.0, 0.0)
    }

    override fun moveToLocation(latLng: uts.sdk.modules.uniMapTencent.latlng.LatLng?, iCallBack: ICallBack) {
        if (latLng != null) {
            setCenter(latLng.latitude, latLng.longitude)
        }else{
            if (currentLocationLatLng.latitude == 0.0 && currentLocationLatLng.longitude == 0.0){
                iCallBack.callback(
                    mapOf(
                        "type" to "fail",
                        "errMsg" to "current location not found"
                    )
                )
                return
            }
            setCenter(currentLocationLatLng.latitude, currentLocationLatLng.longitude)
        }
        iCallBack.callback(
            mapOf(
                "type" to "success",
                "errMsg" to ""
            )
        )
    }

    override fun getRegion(): Bounds {
        val latLngBounds = mapView?.map?.projection?.visibleRegion?.latLngBounds
        return Bounds(
            uts.sdk.modules.uniMapTencent.latlng.LatLng(latLngBounds?.southwest?.latitude ?: 0.0, latLngBounds?.southwest?.longitude ?: 0.0),
            uts.sdk.modules.uniMapTencent.latlng.LatLng(latLngBounds?.northeast?.latitude ?: 0.0, latLngBounds?.northeast?.longitude ?: 0.0)
        )
    }

    override fun translateMarker(
        markerId: Int,
        destination: uts.sdk.modules.uniMapTencent.latlng.LatLng,
        rotate: Double?,
        moveWithRotate: Boolean?,
        duration: Double?,
        callback: ICallBack
    ) {
        markerManager?.translateMarker(markerId, destination, rotate, moveWithRotate, duration, callback)
    }

    override fun moveAlong(markerId: Int, path: List<uts.sdk.modules.uniMapTencent.latlng.LatLng>, duration: Double?, callback: ICallBack) {
        markerManager?.moveAlongMarker(markerId, path, duration ?: 1000.0, callback)
    }

    private fun px2dp(context: Context, pxValue: Float): Float{
        return pxValue /  context.resources.displayMetrics.density
    }

    //<editor-fold desc=”定位”>
    override fun onLocationChanged(p0: TencentLocation?, p1: Int, p2: String?) {
        if (p1 == TencentLocation.ERROR_OK && locationChangedListener != null) {
            currentLocationLatLng.latitude = p0?.latitude ?: 0.0
            currentLocationLatLng.longitude = p0?.longitude ?: 0.0
            val location = Location("")
            location.latitude = p0?.latitude ?: 0.0
            location.longitude = p0?.longitude ?: 0.0
            location.bearing = p0?.bearing ?: 0.0f
            locationChangedListener?.onLocationChanged(location)
            if (!canRegisterMyLocationClick) {
                canRegisterMyLocationClick = true
            }
        }
    }

    override fun onStatusUpdate(p0: String?, p1: Int, p2: String?) {
    }


    private var locationChangedListener: LocationSource.OnLocationChangedListener? = null
    override fun activate(p0: LocationSource.OnLocationChangedListener?) {
        locationChangedListener = p0
        val error = locationManager?.requestLocationUpdates(locationRequest, this)
        when (error) {
            1 -> {
                Log.d("aaa", "设备缺少使用腾讯定位服务需要的基本条件")
            }

            2 -> {
                Log.d("aaa", "manifest 中配置的 key 不正确")
            }

            3 -> {
                Log.d("aaa", "自动加载libtencentloc.so失败")
            }

            else -> {}
        }
    }

    override fun deactivate() {
        locationManager?.removeUpdates(this)
        locationManager = null
        locationRequest = null
        locationChangedListener = null
    }

    //</editor-fold>
}
