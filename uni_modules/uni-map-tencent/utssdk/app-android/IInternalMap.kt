package uts.sdk.modules.uniMapTencent

import android.content.Context
import android.view.View
import uts.sdk.modules.uniMapTencent.latlng.LatLng
import uts.sdk.modules.uniMapTencent.circle.Circle
import uts.sdk.modules.uniMapTencent.control.Control
import uts.sdk.modules.uniMapTencent.ground.Bounds
import uts.sdk.modules.uniMapTencent.ground.Ground
import uts.sdk.modules.uniMapTencent.marker.MarkerModel
import uts.sdk.modules.uniMapTencent.polygon.Polygon
import uts.sdk.modules.uniMapTencent.polyline.Polyline

interface ICallBack {
    fun callback(param: Any?)
}


interface IInternalMap {
    fun getMap(context: Context): View
    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onDestroy()

    /**
     * 设置中心点
     * @param latitude Double
     * @param longitude Double
     */
    fun setCenter(latitude: Double, longitude: Double, animate: Boolean = true)

    /**
     * 设置最小缩放级别
     * @param zoom Int
     */
    fun setMinScale(zoom: Int)

    /**
     * 设置最大缩放级别
     * @param zoom Int
     */
    fun setMaxScale(zoom: Int)

    /**
     * 设置地图缩放级别
     * @param zoom Int
     */
    fun setScale(zoom: Int, animate: Boolean = true)

    /**
     * 获取地图缩放级别
     * @return Int
     */
    fun getScale(): Int

    /**
     * 设置地图主题
     * @param theme String satellite | normal
     */
    fun setTheme(theme: String)

    /**
     * 个性化地图
     * @param id String
     */
    fun setLayerStyle(id: String)

    /**
     * 添加标记
     * @param markerModels List<MapMarker>
     */
    fun addMarkers(markerModels: List<MarkerModel>)

    /**
     * 设置标记点
     * @param markerModels List<MarkerModel>
     */
    fun setMarkers(markerModels: List<MarkerModel>)

    /**
     * 移除 marker
     * @param ids List<Int>
     */
    fun removeMarkers(ids: List<Int>)

    /**
     * 添加折线
     * @param list List<Polyline>
     */
    fun setPolyline(list: List<Polyline>)

    /**
     * 添加圆
     * @param list List<Circle>
     */
    fun setCircle(list: List<Circle>)

    /**
     * 添加控件
     * @param list List<Control>
     */
    fun setControl(list: List<Control>)

    /**
     * 添加多边形
     * @param list List<Polygon>
     */
    fun setPolygon(list: List<Polygon>)

    /**
     * 创建自定义图片图层，图片会随着地图缩放而缩放
     * @param ground Ground
     */
    fun addGroundOverlay(ground: Ground, iCallBack: ICallBack)

    /**
     * 更新自定义图片图层。
     * @param ground Ground
     */
    fun updateGroundOverlay(ground: Ground, iCallBack: ICallBack)


    /**
     * 移除自定义图片图层
     * @param id String
     * @param iCallBack ICallBack
     */
    fun removeGroundOverlay(id: String, iCallBack: ICallBack)

    /**
     * 缩放视野以包含所有给定的坐标点
     * @param points List<LatLng>
     */
    fun setIncludePoints(points: List<LatLng>, animate: Boolean = true)

    /**
     * 是否显示3D楼块
     * @param enable Boolean
     */
    fun enable3D(enable: Boolean)

    /**
     * 是否显示指南针
     * @param enable Boolean
     */
    fun showCompass(enable: Boolean)

    /**
     * 是否支持缩放
     * @param enable Boolean
     */
    fun enableZoom(enable: Boolean)

    /**
     * 是否支持拖动
     * @param enable Boolean
     */
    fun enableScroll(enable: Boolean)

    /**
     * 是否支持旋转
     * @param enable Boolean
     */
    fun enableRotate(enable: Boolean)

    /**
     * 旋转角度(范围0-360)地图正北和设备 y 轴角度的夹角
     * @param rotate Float
     */
    fun setRotate(rotate: Float, animate: Boolean = true)

    /**
     * 倾斜角度，范围 0 ~ 40 , 关于 z 轴的倾角
     * @param skew Float
     */
    fun setSkew(skew: Float, animate: Boolean = true)

    /**
     * 是否开启俯视
     * @param enable Boolean
     */
    fun setEnableOverlooking(enable: Boolean)

    /**
     * 是否开启卫星图
     * @param enable Boolean
     */
    fun setEnableSatellite(enable: Boolean)

    /**
     * 是否开启实时路况
     * @param enable Boolean
     */
    fun setEnableTraffic(enable: Boolean)

    /**
     * 是否展示 POI 点
     * @param enable Boolean
     */
    fun setEnablePoi(enable: Boolean)

    /**
     * 是否展示建筑物
     * @param enable Boolean
     */
    fun setEnableBuilding(enable: Boolean)

    /**
     * 显示带有方向的当前定位点
     * @param show Boolean
     */
    fun setShowLocation(show: Boolean)

    /**
     * 是否展示室内地图
     * @param enable Boolean
     */
    fun setEnableIndoorMap(enable: Boolean)

    /**
     * 在地图渲染更新完成时触发
     * @param iCallBack ICallBack
     */
    fun setOnMapLoadedCallback(iCallBack: ICallBack)

    /**
     * 点击地图时触发
     * @param iCallBack ICallBack
     */
    fun setOnMapClickCallback(iCallBack: ICallBack)

    /**
     * 视野发生变化时触发
     * @param iCallBack ICallBack
     */
    fun setOnMapRegionChange(iCallBack: ICallBack)

    /**
     * 点击控件时触发
     * @param iCallBack ICallBack
     */
    fun setControlTap(iCallBack: ICallBack)

    /**
     * 点击标记时触发
     * @param iCallBack ICallBack
     */
    fun setMyLocationClick(iCallBack: ICallBack)

    /**
     * 点击标记点时触发
     * @param iCallBack ICallBack
     */
    fun setOnMarkerClick(iCallBack: ICallBack)

    /**
     * 点击标记点对应的气泡时触发
     * @param iCallBack ICallBack
     */
    fun setOnCalloutClick(iCallBack: ICallBack)

    /**
     * 点击地图poi点时触发
     * @param iCallBack ICallBack
     */
    fun setPoiTapClick(iCallBack: ICallBack)


    /**
     * 获取当前地图中心的经纬度。返回的是 gcj02 坐标系
     * @return LatLng
     */
    fun getCenterLocation(): LatLng

    /**
     * 将地图中心移动到当前定位点。需要配合map组件的show-location使用
     * @param latLng LatLng?
     */
    fun moveToLocation(latLng: LatLng?, iCallBack: ICallBack)

    /**
     * 获取当前地图的视野范围
     * @return Bounds
     */
    fun getRegion(): Bounds


    /**
     * 平移marker，带动画
     * @param markerId Int
     * @param destination LatLng
     * @param rotate Double?
     * @param moveWithRotate Boolean?
     * @param duration Double?
     * @param callback ICallBack
     */
    fun translateMarker(markerId: Int, destination: LatLng, rotate: Double?, moveWithRotate: Boolean?, duration: Double?, callback: ICallBack)

    /**
     * 沿指定路径移动 marker，用于轨迹回放等场景。动画完成时触发回调事件，若动画进行中，对同一 marker 再次调用 moveAlong 方法，前一次的动画将被打断。
     * @param markerId Int
     * @param path List<LatLng>
     * @param duration Double
     * @param callback ICallBack
     */
    fun moveAlong(markerId: Int, path: List<LatLng>, duration: Double?, callback: ICallBack)
}