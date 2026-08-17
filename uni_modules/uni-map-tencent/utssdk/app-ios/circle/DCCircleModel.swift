//
//  Circle.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation
import UIKit

class DCCircleModel {
    var latitude: Double
    var longitude: Double
    var radius: Double
    var color: UIColor? = nil
    var fillColor: UIColor? = nil
    var strokeWidth: Double? = nil
    
    init(latitude: Double, longitude: Double, radius: Double) {
        self.latitude = latitude
        self.longitude = longitude
        self.radius = radius
    }
}
