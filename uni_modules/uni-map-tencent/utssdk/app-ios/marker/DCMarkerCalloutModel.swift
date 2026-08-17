//
//  DCMarkerCallout.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/12/27.
//

import Foundation
import UIKit
class DCMarkerCalloutModel : Equatable{
    static func == (lhs: DCMarkerCalloutModel, rhs: DCMarkerCalloutModel) -> Bool {
        return lhs.content == rhs.content &&
        lhs.color == rhs.color &&
        lhs.fontSize == rhs.fontSize &&
        lhs.borderRadius == rhs.borderRadius &&
        lhs.borderWidth == rhs.borderWidth &&
        lhs.borderColor == rhs.borderColor &&
        lhs.bgColor == rhs.bgColor &&
        lhs.padding == rhs.padding &&
        lhs.display == rhs.display &&
        lhs.textAlign == rhs.textAlign &&
        lhs.anchorX == rhs.anchorX &&
        lhs.anchorY == rhs.anchorY
    }
    
    var content: String?
    var color: UIColor?
    var fontSize: Double?
    var borderRadius: Double?
    var borderWidth: Double?
    var borderColor: UIColor?
    var bgColor: UIColor?
    var padding: Double?
    var display: Bool?
    var textAlign: String?
    var anchorX: Double?
    var anchorY: Double?
}
