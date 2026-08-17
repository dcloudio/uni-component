//
//  DCGroundOverlayModel.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/9/12.
//

import Foundation
@_implementationOnly import QMapKit

class DCBounds {
    var southwest: DCLatLng
    var northeast: DCLatLng
    
    init(southwest: DCLatLng, northeast: DCLatLng) {
        self.southwest = southwest
        self.northeast = northeast
    }
    
    
    func convertToQBounds() -> QCoordinateBounds{
        return QCoordinateBounds(northEast: northeast.convertToLatlng(), southWest: southwest.convertToLatlng())
    }
}


class DCGroundOverlayModel {
    var id: String
    var src: String
    var bounds: DCBounds
    
    var visible = true
    var zIndex = 0.0
    private var _opacity = 1.0
    var opacity: Double {
        get {
            return _opacity
        }
        set {
            if newValue >= 1 {
                _opacity = 1.0
            } else if newValue <= 0 {
                _opacity = 0.0
            } else {
                _opacity = newValue
            }
        }
    }
    
    init(id: String, src: String, bounds: DCBounds) {
        self.id = id
        self.src = src
        self.bounds = bounds
    }
}
