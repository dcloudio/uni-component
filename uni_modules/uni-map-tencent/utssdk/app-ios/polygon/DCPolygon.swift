//
//  PolygonManager.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation
@_implementationOnly import QMapKit

class DCPolygon : QPolygon {
    var polygonModel: DCPolygonModel
    
    init(withCoordinates coords: UnsafeMutablePointer<CLLocationCoordinate2D>!, count: UInt, polygonModel: DCPolygonModel) {
        self.polygonModel = polygonModel
        super.init(withCoordinates: coords, count: count)
    }
    
}
