//
//  CircleManager.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation
@_implementationOnly import QMapKit
class DCCircle : QCircle {
    var circleModel: DCCircleModel
    
    init(withCenterCoordinate coord: CLLocationCoordinate2D, radius: Double, circleModel: DCCircleModel) {
        self.circleModel = circleModel
        super.init(withCenter: coord, radius: radius)
    }
    
}
