//
//  Polygon.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation
import UIKit

class DCPolygonModel {
    var points: [DCLatLng]
    var strokeWidth: Double? = nil
    var strokeColor: UIColor? = nil
    var fillColor: UIColor? = nil
    var zIndex: Double? = nil
    init(points: [DCLatLng]) {
        self.points = points
    }
}
