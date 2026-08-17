//
//  Control.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/8/8.
//

import Foundation

class DCPosition {
    var left: Double? = nil
    var top: Double? = nil
    var width: Double? = nil
    var height: Double? = nil
}

class DCControlModel {
    var position: DCPosition
    var iconPath: String
    var id: Int? = nil
    var clickable: Bool? = nil
    
    init(position: DCPosition, iconPath: String, id: Int? = nil, clickable: Bool? = nil) {
        self.position = position
        self.iconPath = iconPath
        self.id = id
        self.clickable = clickable
    }
}
