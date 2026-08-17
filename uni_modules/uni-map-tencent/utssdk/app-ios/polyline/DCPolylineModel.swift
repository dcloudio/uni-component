//
//  PolylineManager.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation
import UIKit

class DCPolylineModel {
    var points: [DCLatLng]
    var color : UIColor? = nil
    var width: Double? = nil
    var dottedLine = false
    var arrowLine = false
    var arrowIconPath: String? = nil
    var colorList : [UIColor]? = nil
    
    init(points: [DCLatLng]) {
        self.points = points
    }
}
