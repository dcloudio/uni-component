//
//  LatLng.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation
import CoreLocation

struct DCLatLng: CustomStringConvertible {
    var description: String {
        return "latitude: \(latitude)  longitude: \(longitude)"
    }
    
    var latitude: Double = 0
    var longitude: Double = 0
    
    init(latitude: Double, longitude: Double) {
        self.latitude = latitude
        self.longitude = longitude
    }
    
    func convertToLatlng() -> CLLocationCoordinate2D {
        return CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
}
