//
//  MultiAnnotationView.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/12/30.
//

import Foundation
@_implementationOnly import QMapKit
class MultiAnnotationView: QAnnotationView {
    private static let notificationName = Notification.Name("tencent-map-callout-hide")
    var markerModel: DCMakerModel?{
        willSet {
            if newValue != markerModel {
                cacheCalloutView?.removeFromSuperview()
                cacheCalloutView = nil
            }
        }
    }
    
    fileprivate var calloutViewHide: Bool = true
    
    /**
     marker 点击事件回调
     */
    var markerTapCallback: ((_ : QAnnotationView) -> Void)?
    
    /**
     callout 点击事件回调
     */
    var markerCalloutTapCallback: ((_ : Int) -> Void)?
    
    private lazy var tapGesture = UITapGestureRecognizer(target:self, action: #selector(handleTap))
    private var cacheCalloutView: UIView?
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setup()
    }
    
    override init!(annotation: (any QAnnotation)!, reuseIdentifier: String!) {
        super.init(annotation: annotation, reuseIdentifier: reuseIdentifier)
        setup()
    }
    
    /**
     CalloutView需要在当前View宽度计算完成后重新设置center
     */
    override func layoutSubviews() {
        super.layoutSubviews()
        if let cacheCalloutView = cacheCalloutView {
            cacheCalloutView.center = CGPoint(x: CGRectGetWidth(self.bounds) / 2.0, y: -CGRectGetHeight(cacheCalloutView.bounds) / 2.0)
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self, name: MultiAnnotationView.notificationName, object: nil)
    }
    
    static func hideAllMarkerCallout(ignoreMarkerModel: DCMakerModel? = nil) {
        NotificationCenter.default.post(name: MultiAnnotationView.notificationName, object: nil, userInfo: ["ignoreMarkerModel": ignoreMarkerModel ?? ""])
    }
    
    func setup() {
        self.addGestureRecognizer(tapGesture)
        NotificationCenter.default.addObserver(self, selector: #selector(handleHide(_:)), name: MultiAnnotationView.notificationName, object: nil)
    }
    
    @objc func handleHide(_ notification: Notification) {
        if let userInfo = notification.userInfo, let markerModel = markerModel {
            if let ignoreMarkerModel = userInfo["ignoreMarkerModel"] {
                if ignoreMarkerModel is String, ignoreMarkerModel as! String == "" {
                    showOrHideMarkerCallout(markerModel: markerModel, hide: true)
                } else if ignoreMarkerModel is DCMakerModel, ignoreMarkerModel as! DCMakerModel != markerModel {
                    showOrHideMarkerCallout(markerModel: markerModel, hide: true)
                }
            }
        }
    }
    /**
     尝试显示，如果Callout设置了display:true，就要显示出来。
     */
    func tryDisplay() {
        addCalloutView(markerModel: markerModel!)
        if let callout = markerModel?.callout {
            if let display = callout.display, display {
                cacheCalloutView?.isHidden = false
                calloutViewHide = false
            }
        }
    }
    
    
    
    @objc private func handleTap(gesture: UIGestureRecognizer) {
        if let markerTapCallback = markerTapCallback {
            markerTapCallback(self)
        }
        
        if let markerModel = markerModel {
            if cacheCalloutView != nil {
                showOrHideMarkerCallout(markerModel: markerModel, hide: !calloutViewHide)
            }
            self.superview?.bringSubviewToFront(self)
        }
    }
    
    private func addCalloutView(markerModel: DCMakerModel) {
        let calloutView = createCalloutView(markerModel)
        cacheCalloutView = calloutView
        self.addSubview(calloutView)
    }
    
    fileprivate func createCalloutView(_ markerModel: DCMakerModel) -> UIView {
        let tempMarkerModel = markerModel
        if tempMarkerModel.callout == nil {
            let calloutModel = DCMarkerCalloutModel()
            calloutModel.content = tempMarkerModel.title
            calloutModel.fontSize = 18.0
            calloutModel.borderColor = UIColor.black
            calloutModel.borderWidth = 1.0
            calloutModel.borderRadius = 6.0
            calloutModel.padding = 10
            tempMarkerModel.callout = calloutModel
        }
        
        let calloutView = CalloutView()
        calloutView.markerModel = tempMarkerModel
        calloutView.isHidden = true
        calloutView.isUserInteractionEnabled = true
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleCalloutTapAction))
        calloutView.addGestureRecognizer(tapGesture)
        calloutView.center = CGPoint(x: CGRectGetWidth(self.bounds) / 2.0, y: -CGRectGetHeight(calloutView.bounds) / 2.0)
        return calloutView
    }
    
    func showOrHideMarkerCallout(markerModel: DCMakerModel, hide: Bool) {
        if let cacheCalloutView = cacheCalloutView {
            if let callout = markerModel.callout {
                if let display = callout.display, display {
                    cacheCalloutView.isHidden = false
                    calloutViewHide = false
                    return
                }
            }
            cacheCalloutView.isHidden = hide
            calloutViewHide = hide
        }
    }
    
    @objc func handleCalloutTapAction(gesture: UIGestureRecognizer) {
        if let markerModel = markerModel {
            markerCalloutTapCallback?(markerModel.id)
        }
    }
    
    override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        var result = CGRectContainsPoint(self.bounds, point)
        if result {
            return true
        }
        
        for subview in self.subviews {
            let subviewPoint = subview.convert(point, from: self)
            result = CGRectContainsPoint(subview.bounds, subviewPoint)
            if result {
                return true
            }
        }
        return false
    }
    
}
