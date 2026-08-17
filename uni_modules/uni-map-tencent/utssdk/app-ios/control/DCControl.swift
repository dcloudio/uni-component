//
//  DCControlView.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/9/13.
//

import Foundation
import UIKit

class DCControl: UIButton {
    var controlModel: DCControlModel? = nil
    
    init(frame: CGRect, controlModel: DCControlModel) {
        self.controlModel = controlModel
        super.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }
}
