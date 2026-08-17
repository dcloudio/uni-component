//
//  Polyline.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation
@_implementationOnly import QMapKit

class DCPolyline: QPolyline {
    var polylineModel: DCPolylineModel
    
    init(coordinates coords: UnsafeMutablePointer<CLLocationCoordinate2D>!, count: UInt, polylineModel: DCPolylineModel) {
        self.polylineModel = polylineModel
        super.init(coordinates: coords, count: count)
    }
}
