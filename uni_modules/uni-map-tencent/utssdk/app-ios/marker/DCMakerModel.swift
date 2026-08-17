//
//  MapMarker.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation

class DCMakerModel: Equatable {
    
    static func == (lhs: DCMakerModel, rhs: DCMakerModel) -> Bool {
        return lhs.id == rhs.id &&
        lhs.latitude == rhs.latitude &&
        lhs.longitude == rhs.longitude &&
        lhs.iconPath == rhs.iconPath &&
        lhs.title == rhs.title &&
        lhs.rotate == rhs.rotate &&
        lhs.alpha == rhs.alpha &&
        lhs.width == rhs.width &&
        lhs.height == rhs.height &&
        lhs.clusterId == rhs.clusterId &&
        lhs.ariaLabel == rhs.ariaLabel &&
        lhs.anchor == rhs.anchor &&
        lhs.callout == rhs.callout
    }
    
    var id: Int
    var latitude: Double
    var longitude: Double
    var iconPath: String
    
    var title: String? = nil
    var rotate: Double = 0.0
    var alpha = 1.0
    var width: Double? = nil
    var height: Double? = nil
    var clusterId: Int? = nil
    var ariaLabel: String? = nil
    var anchor:[Float] = [0.5, 1.0]
    var callout: DCMarkerCalloutModel? = nil
    //    var joinCluster = false
    //    var label: MarkerLabel? = nil
    //    var customCallout: MarkerCustomCallout? = nil
    
    init(id: Int, latitude: Double, longitude: Double, iconPath: String) {
        self.id = id
        self.latitude = latitude
        self.longitude = longitude
        self.iconPath = iconPath
    }
    
}
