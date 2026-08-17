//
//  IInternalMap.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation
import UIKit


typealias CallBack = (Any) -> ()

protocol IInternalMap {
    func getMap() -> UIView
    
    func destroy()
    /**
     * 设置中心点
     * @param latitude Double
     * @param longitude Double
     */
    func setCenter(latitude: Double, longitude: Double, animate: Bool)
    
    /**
     * 设置最小缩放级别
     * @param zoom Int
     */
    func setMinScale(zoom: Int)
    
    /**
     * 设置最大缩放级别
     * @param zoom Int
     */
    func setMaxScale(zoom: Int)
    
    /**
     * 设置地图缩放级别
     * @param zoom Int
     */
    func setScale(zoom: Int, animate: Bool)
    
    /**
     * 获取地图缩放级别
     * @return Int
     */
    func getScale() -> Int
    
    /**
     * 设置地图主题
     * @param theme String satellite | normal
     */
    func setTheme(theme: String)
    
    /**
     * 个性化地图
     * @param id String
     */
    func setLayerStyle(id: String)
    
    /**
     * 添加标记
     * @param markers List<MapMarker>
     */
    func addMarkers(markerModels: Array<DCMakerModel>)
    
    /**
     * 设置标记点
     * @param markerModels List<MarkerModel>
     */
    func setMarkers(markerModels: Array<DCMakerModel>)
    
    /**
     * 移除 marker
     * @param ids List<Int>
     */
    func removeMarkers(ids: Array<Int>)
    
    /**
     * 添加折线
     * @param list List<Polyline>
     */
    func setPolyline(list: Array<DCPolylineModel>)
    
    /**
     * 添加圆
     * @param list List<Circle>
     */
    func setCircle(list: Array<DCCircleModel>)
    
    /**
     * 添加控件
     * @param list List<Control>
     */
    func setControl(list: Array<DCControlModel>)
    
    /**
     * 添加多边形
     * @param list List<Polygon>
     */
    func setPolygon(list: Array<DCPolygonModel>)
    
    /**
     创建自定义图片图层，图片会随着地图缩放而缩放
     */
    func addGroundOverlay(model: DCGroundOverlayModel, callback: @escaping CallBack)
    
    /**
     更新自定义图片图层
     */
    func updateGroundOverlay(model: DCGroundOverlayModel, callback: @escaping CallBack)
    
    /**
     移除自定义图片图层
     */
    func removeGroundOverlay(id: String, callback: @escaping CallBack)
    
    /**
     * 缩放视野以包含所有给定的坐标点
     * @param points List<LatLng>
     */
    func setIncludePoints(points: Array<DCLatLng>, animate: Bool)
    
    /**
     * 是否显示3D楼块
     * @param enable Bool
     */
    func enable3D(enable: Bool)
    
    /**
     * 是否显示指南针
     * @param enable Bool
     */
    func showCompass(enable: Bool)
    
    /**
     * 是否支持缩放
     * @param enable Bool
     */
    func enableZoom(enable: Bool)
    
    /**
     * 是否支持拖动
     * @param enable Bool
     */
    func enableScroll(enable: Bool)
    
    /**
     * 是否支持旋转
     * @param enable Bool
     */
    func enableRotate(enable: Bool)
    
    /**
     * 旋转角度(范围0-360)地图正北和设备 y 轴角度的夹角
     * @param rotate Float
     */
    func setRotate(rotate: Float, animate: Bool)
    
    /**
     * 倾斜角度，范围 0 ~ 40 , 关于 z 轴的倾角
     * @param skew Float
     */
    func setSkew(skew: Float, animate: Bool)
    
    /**
     * 是否开启俯视
     * @param enable Bool
     */
    func setEnableOverlooking(enable: Bool)
    
    /**
     * 是否开启卫星图
     * @param enable Bool
     */
    func setEnableSatellite(enable: Bool)
    
    /**
     * 是否开启实时路况
     * @param enable Bool
     */
    func setEnableTraffic(enable: Bool)
    
    /**
     * 是否展示 POI 点
     * @param enable Bool
     */
    func setEnablePoi(enable: Bool)
    
    /**
     * 是否展示建筑物
     * @param enable Bool
     */
    func setEnableBuilding(enable: Bool)
    
    /**
     * 显示带有方向的当前定位点
     * @param show Bool
     */
    func setShowLocation(show: Bool)
    
    /**
     * 是否展示室内地图
     * @param enable Bool
     */
    func setEnableIndoorMap(enable: Bool)
    
    /**
     * 在地图渲染更新完成时触发
     * @param iCallBack ICallBack
     */
    func setOnMapLoadedCallback(iCallBack: @escaping CallBack)
    
    /**
     * 点击地图时触发
     * @param iCallBack ICallBack
     */
    func setOnMapClickCallback(iCallBack: @escaping CallBack)
    
    /**
     * 视野发生变化时触发
     * @param iCallBack ICallBack
     */
    func setOnMapRegionChange(iCallBack: @escaping CallBack)
    
    /**
     * 点击控件时触发
     * @param iCallBack ICallBack
     */
    func setControlTap(iCallBack: @escaping CallBack)
    
    /**
     * 点击标记时触发
     * @param iCallBack ICallBack
     */
    func setMyLocationClick(iCallBack: @escaping CallBack)
    
    /**
     点击标记点时触发
     */
    func setOnMarkerClick(iCallBack: @escaping CallBack)
    
    /**
     点击标记点对应的气泡时触发
     */
    func setOnCalloutClick(iCallBack: @escaping CallBack)
    
    /**
     * 点击地图poi点时触发
     * @param iCallBack ICallBack
     */
    func setPoiTapClick(iCallBack: @escaping CallBack)
    
    /**
     * 获取当前地图中心的经纬度。返回的是 gcj02 坐标系
     * @return LatLng
     */
    func getCenterLocation() -> DCLatLng
    
    /**
     * 将地图中心移动到当前定位点。需要配合map组件的show-location使用
     * @param latLng LatLng?
     */
    func moveToLocation(latLng: DCLatLng?, iCallBack: @escaping CallBack)
    
    /**
     * 获取当前地图的视野范围
     * @return Bounds
     */
    func getRegion() -> DCBounds
    
    
    /**
     * 平移marker，带动画
     * @param markerId Int
     * @param destination LatLng
     * @param rotate Double?
     * @param moveWithRotate Boolean?
     * @param duration Double?
     * @param callback ICallBack
     */
    func translateMarker(markerId: Int, destination: DCLatLng, rotate: Double?, moveWithRotate: Bool?, duration: Double?, callback: @escaping CallBack)
    
    /**
     * 沿指定路径移动 marker，用于轨迹回放等场景。动画完成时触发回调事件，若动画进行中，对同一 marker 再次调用 moveAlong 方法，前一次的动画将被打断。
     * @param markerId Int
     * @param path List<LatLng>
     * @param duration Double
     * @param callback ICallBack
     */
    func moveAlong(markerId: Int, path: [DCLatLng], duration: Double?, callback: @escaping CallBack)
}
