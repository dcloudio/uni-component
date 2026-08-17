//
//  AnimationLocation.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/9/19.
//

import Foundation
@_implementationOnly import QMapSDKUtils

class AnimationLocation: NSObject, QMULocation {
    var coordinate: CLLocationCoordinate2D
    
    init(coordinate: CLLocationCoordinate2D) {
        self.coordinate = coordinate
    }
}
