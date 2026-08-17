//
//  TencentMapImpl.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation
import DCloudUniappRuntime
@_implementationOnly import QMapKit
@_implementationOnly import QMapSDKUtils

class TencentMapImpl : NSObject, IInternalMap, QMapViewDelegate {
    
    private var mapView: QMapView? = nil
    private var mCameraType = "drag"
    private var isSetUpdate = false
    private var mZoomLevel = 16.0
    private var isChangeStart = false
    
    private var mPolylines: [QPolyline] = []
    private var mPolygons: [QPolygon] = []
    private var mCircles: [QCircle] = []
    private var mGroundCaches: [String: (QGroundOverlay ,DCGroundOverlayModel)] = [:]
    private var mControls: [DCControl] = []
    private var mMarkers: [Int: DCMarker] = [:]
    
    private var mapLoadedCallback: CallBack? = nil
    private var mapClickCallback: CallBack? = nil
    private var mapRegionChangeCallback: CallBack? = nil
    private var mapControlTapCallback: CallBack? = nil
    private var mapMarkerTapCallback: CallBack? = nil
    private var mapMyLocationTapCallback: CallBack? = nil
    private var mapPoiTapCallback: CallBack? = nil
    private var mapCalloutTapCallback: CallBack? = nil
    
    func getMap() -> UIView {
        mapView = QMapView()
        defaultConfig()
        return mapView!
    }
    
    func destroy() {
        mapView?.removeOverlays(mPolylines)
        mapView?.removeOverlays(mPolygons)
        mapView?.removeOverlays(mCircles)
        mGroundCaches.forEach { (key: String, value: (groundOverlay: QGroundOverlay,model: DCGroundOverlayModel)) in
            mapView?.remove(value.groundOverlay)
        }
        mControls.forEach { control in
            control.removeFromSuperview()
        }
        clearMarkers()
        mPolylines.removeAll()
        mPolygons.removeAll()
        mCircles.removeAll()
        mGroundCaches.removeAll()
        mControls.removeAll()
        mMarkers.removeAll()
        mapView?.delegate = nil
        mapView = nil
    }
    
    
    deinit {
        destroy()
    }
    
    private func defaultConfig() {
        mapView?.delegate = self
    }
    
    
    func setCenter(latitude: Double, longitude: Double, animate: Bool = true) {
        isSetUpdate = true
        mapView?.setCenter(CLLocationCoordinate2D(latitude: latitude, longitude: longitude), animated: animate)
    }
    
    func setMinScale(zoom: Int) {
        let maxZoomLevel = mapView?.maxZoomLevel
        mapView?.setMinZoomLevel(CGFloat(zoom), maxZoomLevel: maxZoomLevel ?? 20)
    }
    
    func setMaxScale(zoom: Int) {
        let minZoomLevel = mapView?.minZoomLevel
        mapView?.setMinZoomLevel(minZoomLevel ?? 3, maxZoomLevel: CGFloat(zoom))
    }
    
    func setScale(zoom: Int, animate: Bool = true) {
        isSetUpdate = true
        mapView?.setZoomLevel(CGFloat(zoom), animated: animate)
    }
    
    func getScale() -> Int {
        return Int(mapView?.zoomLevel ?? 0)
    }
    
    func setTheme(theme: String) {
        if theme == "satellite" {
            mapView?.mapType = QMapType.satellite
        } else {
            mapView?.mapType = QMapType.standard
        }
    }
    
    func setLayerStyle(id: String) {
        let defaultStyle = Int32(mapView?.mapType.rawValue ?? 0)
        mapView?.setMapStyle(Int32(id) ?? defaultStyle)
    }
    
    func addMarkers(markerModels markers: Array<DCMakerModel>) {
        
        var addMarkers: [DCMarker] = []
        var oldMarkers: [DCMarker] = []
        
        for model in markers {
            if let marker = mMarkers[model.id] {
                oldMarkers.append(marker)
                mMarkers.removeValue(forKey: model.id)
            }
            let marker = DCMarker(markerModel: model)
            marker.coordinate = CLLocationCoordinate2D(latitude: model.latitude, longitude: model.longitude)
            if let title = model.title {
                marker.title = title
            }
            mMarkers[model.id] = marker
            addMarkers.append(marker)
        }
        if !oldMarkers.isEmpty {
            mapView?.removeAnnotations(oldMarkers)
        }
        if !addMarkers.isEmpty {
            mapView?.addAnnotations(addMarkers)
        }
    }
    
    func setMarkers(markerModels: Array<DCMakerModel>) {
        clearMarkers()
        addMarkers(markerModels: markerModels)
    }
    
    func removeMarkers(ids: Array<Int>) {
        let filterDic = mMarkers.filter { (key: Int, value: DCMarker) in
            return ids.contains(key)
        }
        let filterMarkerKeys = Array(filterDic.keys)
        let filterMarkers = Array(filterDic.values)
        filterMarkerKeys.forEach { key in
            mMarkers.removeValue(forKey: key)
        }
        mapView?.removeAnnotations(filterMarkers)
    }
    
    private func clearMarkers(){
        mapView?.removeAnnotations(Array(mMarkers.values))
        mMarkers.removeAll()
    }
    
    func setPolyline(list: Array<DCPolylineModel>) {
        mapView?.removeOverlays(mPolylines)
        mPolylines.removeAll()
        let result = list.map { model in
            var points = model.points.map { latlng in
                return CLLocationCoordinate2D(latitude: latlng.latitude, longitude: latlng.longitude)
            }
            let polyline = DCPolyline(coordinates: &points, count: UInt(points.count), polylineModel: model)
            return polyline
        }
        mPolylines.append(contentsOf: result)
        mapView?.addOverlays(result)
    }
    
    func setCircle(list: Array<DCCircleModel>) {
        mapView?.removeOverlays(mCircles)
        mCircles.removeAll()
        let result = list.map { model in
            let latlng = CLLocationCoordinate2D(latitude: model.latitude, longitude: model.longitude)
            let polyline = DCCircle(withCenterCoordinate: latlng, radius: model.radius, circleModel: model)
            return polyline
        }
        mCircles.append(contentsOf: result)
        mapView?.addOverlays(result)
    }
    
    func setControl(list: Array<DCControlModel>) {
        mControls.forEach { control in
            control.removeFromSuperview()
        }
        mControls.removeAll()
        
        list.forEach { model in
            let control = DCControl(frame: CGRectMake(model.position.left ?? 0, model.position.top ?? 0, model.position.width ?? 0, model.position.height ?? 0), controlModel: model)
            if model.clickable == true {
                control.addTarget(self, action: #selector(handleControlClick), for: .touchUpInside)
            }
            
            UTSiOS.loadImage(model.iconPath) { [weak self] image, data in
                if let that = self {
                    control.setBackgroundImage(image, for: .normal)
                    that.mapView?.addSubview(control)
                    that.mControls.append(control)
                }
            }
        }
    }
    
    @objc func handleControlClick(control: DCControl) {
        mapControlTapCallback?(["type":"controltap", "detail": ["controlId": control.controlModel?.id ?? control.hashValue]])
    }
    
    func setPolygon(list: Array<DCPolygonModel>) {
        mapView?.removeOverlays(mPolygons)
        mPolygons.removeAll()
        let result = list.map { model in
            var points = model.points.map { latlng in
                return CLLocationCoordinate2D(latitude: latlng.latitude, longitude: latlng.longitude)
            }
            let polygon = DCPolygon(withCoordinates: &points, count: UInt(points.count), polygonModel: model)
            return polygon
        }
        mPolygons.append(contentsOf: result)
        mapView?.addOverlays(result)
    }
    
    func addGroundOverlay(model: DCGroundOverlayModel, callback: @escaping CallBack) {
        updateGroundOverlay(model: model, callback: callback)
    }
    
    func updateGroundOverlay(model: DCGroundOverlayModel, callback: @escaping CallBack) {
        UTSiOS.loadImage(model.src) { [weak self] image, data in
            if image == nil {
                callback(["type":"fail", "errMsg": "network image loading failed"])
                return
            }
            if let that = self {
                if that.mGroundCaches.keys.contains(model.id) {
                    let groundOverlay = that.mGroundCaches[model.id]!.0
                    groundOverlay.visible = model.visible
                    groundOverlay.opacity = model.opacity
                    that.mGroundCaches[model.id] = (groundOverlay, model)
                    groundOverlay.setGroundOverlayWith(model.bounds.convertToQBounds(), icon: image ?? UIImage())
                }else {
                    let groundOverlay = QGroundOverlay(bounds: model.bounds.convertToQBounds(), icon: image ?? UIImage())
                    groundOverlay.visible = model.visible
                    groundOverlay.opacity = model.opacity
                    that.mGroundCaches[model.id] = (groundOverlay, model)
                    that.mapView?.add(groundOverlay)
                }
                callback(["type":"success", "errMsg": ""])
            }
        }
    }
    
    func removeGroundOverlay(id: String, callback: @escaping CallBack) {
        if mGroundCaches.keys.contains(id) {
            mapView?.remove(mGroundCaches.removeValue(forKey: id)?.0)
            callback(["type":"success", "errMsg": ""])
        } else {
            callback(["type":"fail", "errMsg": "id not found"])
        }
    }
    
    func setIncludePoints(points: Array<DCLatLng>, animate: Bool = true) {
        if points.isEmpty {
            return
        }
        isSetUpdate = true
        var pointList = points.map { latlng in
            return CLLocationCoordinate2D(latitude: latlng.latitude, longitude: latlng.longitude)
        }
        let region = QBoundingCoordinateRegionWithCoordinates(&pointList, UInt(pointList.count))
        mapView?.setRegion(region, animated: animate)
    }
    
    func enable3D(enable: Bool) {
        mapView?.showsBuildings = enable
        mapView?.shows3DBuildings = enable
    }
    
    func showCompass(enable: Bool) {
        mapView?.showsCompass = enable
    }
    
    func enableZoom(enable: Bool) {
        mapView?.isZoomEnabled = enable
    }
    
    func enableScroll(enable: Bool) {
        mapView?.isScrollEnabled = enable
    }
    
    func enableRotate(enable: Bool) {
        mapView?.isRotateEnabled = enable
    }
    
    func setRotate(rotate: Float, animate: Bool = true) {
        isSetUpdate = true
        mapView?.setRotation(CGFloat(rotate), animated: animate)
    }
    
    func setSkew(skew: Float, animate: Bool = true) {
        isSetUpdate = true
        mapView?.setOverlooking(CGFloat(skew), animated: animate)
    }
    
    func setEnableOverlooking(enable: Bool) {
        mapView?.isOverlookingEnabled = enable
    }
    
    func setEnableSatellite(enable: Bool) {
        setTheme(theme: enable ? "satellite" : "")
    }
    
    func setEnableTraffic(enable: Bool) {
        mapView?.showsTraffic = enable
    }
    
    func setEnablePoi(enable: Bool) {
        mapView?.showsPoi = enable
    }
    
    func setEnableBuilding(enable: Bool) {
        enable3D(enable: enable)
    }
    
    func setShowLocation(show: Bool) {
        mapView?.showsUserLocation = show
    }
    
    func setEnableIndoorMap(enable: Bool) {
        mapView?.setIndoorEnabled(enable)
    }
    
    func setOnMapLoadedCallback(iCallBack: @escaping CallBack) {
        mapLoadedCallback = iCallBack
    }
    
    func setOnMapClickCallback(iCallBack: @escaping CallBack) {
        mapClickCallback = iCallBack
    }
    
    func setOnMapRegionChange(iCallBack: @escaping CallBack) {
        mapRegionChangeCallback = iCallBack
    }
    
    func setControlTap(iCallBack: @escaping CallBack) {
        mapControlTapCallback = iCallBack
    }
    
    func setMyLocationClick(iCallBack: @escaping CallBack) {
        mapMyLocationTapCallback = iCallBack
    }
    
    func setOnMarkerClick(iCallBack: @escaping CallBack) {
        mapMarkerTapCallback = iCallBack
    }
    
    func setOnCalloutClick(iCallBack: @escaping CallBack) {
        mapCalloutTapCallback = iCallBack
    }
    
    func setPoiTapClick(iCallBack: @escaping CallBack) {
        mapPoiTapCallback = iCallBack
    }
    
    func getCenterLocation() -> DCLatLng {
        return DCLatLng(latitude: mapView?.centerCoordinate.latitude ?? 0, longitude: mapView?.centerCoordinate.longitude ?? 0)
    }
    
    func moveToLocation(latLng: DCLatLng?, iCallBack: @escaping CallBack) {
        if let latlng = latLng {
            setCenter(latitude: latlng.latitude, longitude: latlng.longitude)
        } else {
            let failCallback = {
                iCallBack(["type":"fail", "errMsg":"current location not found"])
            }
            if let myLocationLatLng = mapView?.userLocation?.location?.coordinate{
                if myLocationLatLng.latitude == 0 || myLocationLatLng.longitude == 0{
                    failCallback()
                    return
                } else {
                    setCenter(latitude: myLocationLatLng.latitude, longitude: myLocationLatLng.longitude)
                }
            } else {
                failCallback()
                return
            }
        }
        iCallBack(["type":"success", "errMsg": ""])
    }
    
    func getRegion() -> DCBounds {
        if let mapView = mapView {
            let latDelta = mapView.region.span.latitudeDelta / 2
            let lngDelta = mapView.region.span.longitudeDelta / 2
            let centerLatLng = mapView.region.center
            let southwest = DCLatLng(latitude: centerLatLng.latitude - latDelta, longitude: centerLatLng.longitude - lngDelta)
            let northeast = DCLatLng(latitude: centerLatLng.latitude + latDelta, longitude: centerLatLng.longitude + lngDelta)
            return DCBounds(southwest: southwest, northeast: northeast)
        } else {
            return DCBounds(southwest: DCLatLng(latitude: 0, longitude: 0), northeast: DCLatLng(latitude: 0, longitude: 0 ))
        }
    }
    
    func translateMarker(markerId: Int, destination: DCLatLng, rotate: Double?, moveWithRotate: Bool?, duration: Double?, callback: @escaping CallBack) {
        if let marker = mMarkers[markerId] {
            if let markerView = mapView?.view(for: marker) {
                if let markerViewLayer = markerView.layer as? QAnnotationViewLayer {
                    if rotate != nil {
                        rotateAndTranslateAnimation(marker: marker, markerViewLayer: markerViewLayer, destination: destination, rotate: rotate!, moveWithRotate: moveWithRotate ?? false, duration: duration ?? 1000.0)
                    } else {
                        translateAnimation(marker: marker, markerViewLayer: markerViewLayer, destination: destination, duration: duration ?? 1000.0)
                    }
                    callback(["type":"success", "errMsg":""])
                }
            }
        } else {
            callback(["type":"fail", "errMsg": "marker not found"])
        }
    }
    
    private func rotateAndTranslateAnimation(marker:DCMarker, markerViewLayer: QAnnotationViewLayer, destination: DCLatLng, rotate: Double, moveWithRotate: Bool, duration: Double) {
        if moveWithRotate {
            let translateAnimation = CAKeyframeAnimation(keyPath: "coordinate")
            let path = CGMutablePath()
            path.move(to: CGPoint(x: marker.coordinate.latitude, y: marker.coordinate.longitude))
            path.addLine(to: CGPoint(x: destination.latitude, y: destination.longitude))
            translateAnimation.path = path
            translateAnimation.duration = duration / 1000.0
            translateAnimation.calculationMode = .paced
            
            let rotationAnimation = CABasicAnimation(keyPath: "transform.rotation.z")
            rotationAnimation.toValue = NSNumber(value: Double.pi / 180.0 * rotate)
            rotationAnimation.duration = duration / 1000.0
            rotationAnimation.repeatCount = 1
            rotationAnimation.fillMode = .forwards
            rotationAnimation.isRemovedOnCompletion = false
            
            let animationGroup = CAAnimationGroup()
            animationGroup.animations = [rotationAnimation, translateAnimation]
            animationGroup.duration = duration / 1000.0
            animationGroup.fillMode = .forwards
            animationGroup.isRemovedOnCompletion = false
            markerViewLayer.add(animationGroup, forKey: "groupAnimation")
        } else {
            let rotationAnimationDuration = duration * 0.3 / 1000.0
            let translateAnimationDuration = duration * 0.7 / 1000.0
            
            let rotationAnimation = CABasicAnimation(keyPath: "transform.rotation.z")
            rotationAnimation.toValue = NSNumber(value: Double.pi / 180.0 * rotate)
            rotationAnimation.duration = rotationAnimationDuration
            rotationAnimation.repeatCount = 1
            rotationAnimation.fillMode = .forwards
            rotationAnimation.isRemovedOnCompletion = false
            
            let translateAnimation = CAKeyframeAnimation(keyPath: "coordinate")
            let path = CGMutablePath()
            path.move(to: CGPoint(x: marker.coordinate.latitude, y: marker.coordinate.longitude))
            path.addLine(to: CGPoint(x: destination.latitude, y: destination.longitude))
            translateAnimation.path = path
            translateAnimation.duration = translateAnimationDuration
            translateAnimation.calculationMode = .paced
            translateAnimation.beginTime = rotationAnimationDuration
            
            let animationGroup = CAAnimationGroup()
            animationGroup.animations = [rotationAnimation, translateAnimation]
            animationGroup.duration = duration / 1000.0
            animationGroup.fillMode = .forwards
            animationGroup.isRemovedOnCompletion = false
            markerViewLayer.add(animationGroup, forKey: "groupAnimation")
        }
    }
    
    private func translateAnimation(marker:DCMarker, markerViewLayer: QAnnotationViewLayer, destination: DCLatLng, duration: Double) {
        let translateAnimation = CAKeyframeAnimation(keyPath: "coordinate")
        let path = CGMutablePath()
        path.move(to: CGPoint(x: marker.coordinate.latitude, y: marker.coordinate.longitude))
        path.addLine(to: CGPoint(x: destination.latitude, y: destination.longitude))
        translateAnimation.path = path
        translateAnimation.duration = duration / 1000.0
        translateAnimation.calculationMode = .paced
        markerViewLayer.add(translateAnimation, forKey: "translateAnimation")
    }
    
    func moveAlong(markerId: Int, path: [DCLatLng], duration: Double?, callback: @escaping CallBack) {
        if let marker = mMarkers[markerId] {
            if let markerView = mapView?.view(for: marker) {
                if let markerViewLayer = markerView.layer as? QAnnotationViewLayer {
                    markerViewLayer.removeAnimation(forKey: "coordinate")
                    markerViewLayer.removeAnimation(forKey: "transform.rotation.z")
                    markerViewLayer.removeAnimation(forKey: "translateAnimation")
                    markerViewLayer.removeAnimation(forKey: "groupAnimation")
                }
                
                let locations = path.map { latlng in
                    return AnimationLocation(coordinate: CLLocationCoordinate2D(latitude: latlng.latitude, longitude: latlng.longitude))
                }
                QMUAnnotationAnimator.translate(with: markerView, locations: locations, duration: (duration ?? 1000.0) / 1000.0, rotateEnabled:true)
                callback(["type":"success", "errMsg":""])
            }
        } else {
            callback(["type":"fail", "errMsg": "marker not found"])
        }
    }
    
    private func resizeImage(image: UIImage, size: CGSize) -> UIImage? {
        if size.width.isZero || size.height.isZero {
            return nil
        }
        UIGraphicsBeginImageContextWithOptions(size, false, UIScreen.main.scale)
        defer {
            UIGraphicsEndImageContext()
        }
        image.draw(in: CGRectMake(0, 0, size.width, size.height))
        return UIGraphicsGetImageFromCurrentImageContext()
    }
    
    private func getKeyWindow() -> UIWindow? {
        var keyWindow: UIWindow?
        
        if let delegateWindow = UIApplication.shared.delegate?.window {
            keyWindow = delegateWindow
        }
        
        if keyWindow == nil {
            if #available(iOS 13.0, *) {
                for scene in UIApplication.shared.connectedScenes {
                    if let windowScene = scene as? UIWindowScene,
                       scene.activationState == .foregroundActive {
                        if #available(iOS 15.0, *) {
                            keyWindow = windowScene.keyWindow
                        } else {
                            keyWindow = windowScene.windows.first
                        }
                        break
                    }
                }
            }
        }
        
        if keyWindow == nil {
            keyWindow = UIApplication.shared.windows.first
        }
        
        if keyWindow == nil {
            keyWindow = UIApplication.shared.keyWindow
        }
        
        return keyWindow
    }
    
    func hideAllMarkerCallout(ignoreMarkerModel: DCMakerModel? = nil) {
        MultiAnnotationView.hideAllMarkerCallout(ignoreMarkerModel: ignoreMarkerModel)
    }
    
    //MARK: delegate
    func mapViewInitComplete(_ mapView: QMapView!) {
        mapLoadedCallback?(["type":"updated", "detail":[String:Any]()])
    }
    
    func mapView(_ mapView: QMapView!, didTapAt coordinate: CLLocationCoordinate2D) {
        hideAllMarkerCallout()
        let clientPoint = mapView.convert(coordinate, toPointTo: mapView)
        let screenPoint = mapView.convert(coordinate, toPointTo: getKeyWindow())
        mapClickCallback?(["type":"tap", "detail": ["latitude": coordinate.latitude,
                                                    "longitude": coordinate.longitude,
                                                    "x": clientPoint.x,
                                                    "y": clientPoint.y,
                                                    "screenX": screenPoint.x,
                                                    "screenY": screenPoint.y,
                                                   ]])
    }
    
    func mapView(_ mapView: QMapView!, regionWillChangeAnimated animated: Bool, gesture bGesture: Bool) {
        if !isChangeStart {
            mCameraType = bGesture ? "gesture" : "update"
            mZoomLevel = mapView.zoomLevel
            mapRegionChangeCallback?(["type": "begin", "detail": [String: Any]()])
            isChangeStart = true
        }
    }
    
    func mapView(_ mapView: QMapView!, regionDidChangeAnimated animated: Bool, gesture bGesture: Bool) {
        if isChangeStart {
            isChangeStart = false
            if bGesture {
                isSetUpdate = false
            }
            
            if isSetUpdate {
                mCameraType = "update"
            } else if mapView.zoomLevel != mZoomLevel {
                mCameraType = "scale"
            } else {
                mCameraType = "drag"
            }
            mapRegionChangeCallback?(["type": "end", "causedBy": mCameraType, "detail": ["skew": mapView.overlooking, "rotate": mapView.rotation]])
            isSetUpdate = false
        }
    }
    
    func mapView(_ mapView: QMapView, viewFor overlay: (any QOverlay)) -> QOverlayView! {
        if overlay is QPolyline {
            guard let polyline = overlay as? DCPolyline else { return nil }
            let model = polyline.polylineModel
            
            var polylineView: QPolylineView? = nil
            
            if model.colorList != nil || model.arrowLine {
                let texturePolylineView = QTexturePolylineView(polyline: polyline)!
                texturePolylineView.drawType = .colorLine
                if model.colorList != nil && !model.colorList!.isEmpty {
                    texturePolylineView.useGradient = true
                    let unit = model.points.count / model.colorList!.count
                    var segmentsColor: [QSegmentColor] = []
                    for (index, color) in model.colorList!.enumerated() {
                        let segColor = QSegmentColor()
                        segColor.startIndex = Int32(index * unit)
                        segColor.endIndex = Int32(min((index + 1), model.points.count) * unit)
                        segColor.color = color
                        segmentsColor.append(segColor)
                    }
                    texturePolylineView.segmentColor = segmentsColor
                }
                polylineView = texturePolylineView
            } else {
                polylineView = QPolylineView(polyline: polyline)
            }
            
            if model.color != nil {
                polylineView?.strokeColor = model.color
            } else {
                polylineView?.strokeColor = UIColor(red: 45/255.0, green: 215/255.0, blue: 255/255.0, alpha: 1.000)
            }
            
            if model.width != nil {
                polylineView?.lineWidth = model.width!
            }
            
            if model.dottedLine {
                polylineView?.lineDashPattern = [NSNumber(value: 25), NSNumber(value: 14)]
            } else {
                polylineView?.lineDashPattern = nil
            }
            
            if model.arrowLine && polylineView is QTexturePolylineView{
                let texturePolylineView = polylineView as! QTexturePolylineView
                texturePolylineView.lineDashPattern = nil
                texturePolylineView.segmentColor = []
                if let path = Bundle.main.path(forResource: model.arrowIconPath, ofType: nil) {
                    texturePolylineView.symbolImage = UIImage(contentsOfFile: path)
                }
                texturePolylineView.isDrawSymbol = true
                texturePolylineView.symbolGap = 30
            }
            return polylineView
        } else if overlay is QPolygon {
            guard let polygon = overlay as? DCPolygon else { return nil }
            let model = polygon.polygonModel
            
            let polygonView = QPolygonView(polygon: polygon)
            if model.strokeWidth != nil {
                polygonView?.lineWidth = model.strokeWidth!
            }
            if model.strokeColor != nil {
                polygonView?.strokeColor = model.strokeColor
            }
            if model.fillColor != nil {
                polygonView?.fillColor = model.fillColor
            }
            if model.zIndex != nil {
                polygonView?.zIndex = Int32(model.zIndex!)
            }
            return polygonView
        } else if overlay is QCircle {
            guard let circle = overlay as? DCCircle else { return nil }
            let model = circle.circleModel
            let circleView = QCircleView(circle: circle)
            if model.color != nil {
                circleView?.strokeColor = model.color
            }
            if model.fillColor != nil {
                circleView?.fillColor = model.fillColor
            }
            if model.strokeWidth != nil {
                circleView?.lineWidth = model.strokeWidth!
            }
            return circleView
        } else if overlay is QGroundOverlay {
            let groundOverlay = overlay as! QGroundOverlay
            let targetDic = mGroundCaches.filter { (key: String, value: (ground: QGroundOverlay, groundModel: DCGroundOverlayModel)) in
                return groundOverlay === value.ground
            }
            if targetDic.count > 0 {
                if let model = targetDic.values.first?.1 {
                    let groundOverlayView = QGroundOverlayView(overlay: groundOverlay)
                    groundOverlayView.zIndex = Int32(model.zIndex)
                    return groundOverlayView
                } else {
                    return nil
                }
            } else {
                return nil
            }
        }
        return nil
    }
    
    func mapView(_ mapView: QMapView!, viewFor annotation: (any QAnnotation)!) -> QAnnotationView! {
        if annotation is QPointAnnotation {
            guard let marker = annotation as? DCMarker else { return nil }
            let model = marker.markerModel
            var markerView = mapView.dequeueReusableAnnotationView(withIdentifier: "reuseId") as? MultiAnnotationView
            if markerView == nil {
                markerView = MultiAnnotationView(annotation: annotation, reuseIdentifier: "reuseId")
            }
            if model.rotate != 0 {
                markerView?.transform = CGAffineTransformMakeRotation(Double.pi / 180.0 * model.rotate)
            }
            
            markerView?.alpha = model.alpha
            
            if model.ariaLabel != nil {
                markerView?.isAccessibilityElement = true
                markerView?.accessibilityLabel = model.ariaLabel
            }
            
            UTSiOS.loadImage(model.iconPath) { [weak self] image, data in
                if let that = self {
                    if image != nil {
                        var image = image
                        if model.width != nil && model.height != nil {
                            image = that.resizeImage(image: image!, size: CGSizeMake(model.width!, model.height!))
                        }
                        markerView?.image = image
                    } else {
                        if let bundle = Bundle.main.path(forResource: "uni-map-tencent", ofType: "bundle") {
                            if let imagePath = Bundle(path: bundle)?.path(forResource: "uni_app_map_marker_ic", ofType: "png") {
                                var image = UIImage(contentsOfFile: imagePath)
                                if model.width != nil && model.height != nil {
                                    image = that.resizeImage(image: image!, size: CGSizeMake(model.width!, model.height!))
                                }
                                markerView?.image = image
                            }
                        }
                    }
                    
                    let width = markerView?.image?.size.width ?? 0
                    let height = markerView?.image?.size.height ?? 0
                    let x = CGFloat(model.anchor[0] - 0.5) * width
                    let y = CGFloat(model.anchor[1] - 0.5) * height
                    markerView?.centerOffset = CGPointMake(-x , -y)
                }
            }
            markerView?.markerModel = model
            markerView?.tryDisplay()
            markerView?.canShowCallout = true
            markerView?.markerTapCallback = { [weak self] markerView  in
                self?.hideAllMarkerCallout(ignoreMarkerModel: (markerView.annotation as? DCMarker)?.markerModel)
                if let self = self, let callback = self.mapMarkerTapCallback {
                    if let marker = markerView.annotation as? DCMarker {
                        let model = marker.markerModel
                        let coordinate = CLLocationCoordinate2D(latitude: model.latitude, longitude: model.longitude)
                        let clientPoint = mapView.convert(coordinate, toPointTo: mapView)
                        let screenPoint = mapView.convert(coordinate, toPointTo: self.getKeyWindow())
                        callback(["type":"markertap", "detail":["markerId": model.id,
                                                               "x": clientPoint.x,
                                                               "y": clientPoint.y,
                                                               "screenX": screenPoint.x,
                                                               "screenY": screenPoint.y,
                                                              ]])
                    }
                }
            }
            markerView?.markerCalloutTapCallback = { [weak self] markerId in
                if let self = self, let callback = self.mapCalloutTapCallback {
                    callback(["type":"callouttap", "detail":["markerId": markerId]])
                }
            }
            
            return markerView
        }
        
        return nil
    }
    
    func mapView(_ mapView: QMapView!, didTapMyLocation location: CLLocationCoordinate2D) {
        if let callback = mapMyLocationTapCallback {
            let clientPoint = mapView.convert(location, toPointTo: mapView)
            let screenPoint = mapView.convert(location, toPointTo: getKeyWindow())
            callback(["type":"anchorpointtap", "detail": ["latitude": location.latitude,
                                                          "longitude": location.longitude,
                                                          "x": clientPoint.x,
                                                          "y": clientPoint.y,
                                                          "screenX": screenPoint.x,
                                                          "screenY": screenPoint.y,
                                                         ]])
        }
    }
    
    func mapView(_ mapView: QMapView!, didTapPoi poi: QPoiInfo!) {
        if let callback = mapPoiTapCallback {
            let clientPoint = mapView.convert(poi.coordinate, toPointTo: mapView)
            let screenPoint = mapView.convert(poi.coordinate, toPointTo: getKeyWindow())
            callback(["type":"poitap", "detail":["latitude":poi.coordinate.latitude, 
                                                 "longitude":poi.coordinate.longitude,
                                                 "name":poi.name ?? "",
                                                 "x": clientPoint.x,
                                                 "y": clientPoint.y,
                                                 "screenX": screenPoint.x,
                                                 "screenY": screenPoint.y,
                                                ]])
        }
    }
}
