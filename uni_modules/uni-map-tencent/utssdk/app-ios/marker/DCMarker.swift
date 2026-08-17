//
//  MarkerManager.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/9/9.
//

import Foundation
@_implementationOnly import QMapKit

class DCMarker: QPointAnnotation {
    var markerModel: DCMakerModel
    init(markerModel: DCMakerModel) {
        self.markerModel = markerModel
    }
}
