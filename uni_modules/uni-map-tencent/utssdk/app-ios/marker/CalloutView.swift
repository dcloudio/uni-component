//
//  CalloutView.swift
//  TencentMap
//
//  Created by dcloud-thb on 2024/12/27.
//

import Foundation
import UIKit

class CalloutView : UIView {
    private lazy var titleLabel: UILabel = {
        let label = UILabel(frame: CGRect.zero)
        label.isUserInteractionEnabled = true
        return label
    }()
    
    private var _borderRadius: Double?
    private var _borderWidth: Double?
    private var _borderColor: UIColor?
    private var _bgColor: UIColor?
    private var _padding: Double?
    
    
    var markerModel: DCMakerModel? {
        didSet {
            if markerModel != nil {
                let callout = markerModel!.callout
                if callout != nil {
                    _borderRadius = callout?.borderRadius ?? 0
                    _borderWidth = callout?.borderWidth ?? 0
                    _borderColor = callout?.borderColor ?? UIColor.clear
                    _bgColor = callout?.bgColor ?? UIColor.white
                    _padding = callout?.padding ?? 0
                    
                    var titleSize = CGSizeZero
                    var calloutRect = CGRect(x: 0, y: 0, width: 50, height: 50)
                    if let content = callout?.content {
                        if !content.isEmpty {
                            let screenWidth = UIScreen.main.bounds.size.width
                            let rect = (content as NSString).boundingRect(with: CGSizeMake(screenWidth - (_padding! * 2 + _borderWidth! * 2), CGFloat(MAXFLOAT)), options: [.usesLineFragmentOrigin, .usesFontLeading], attributes: [.font: UIFont.systemFont(ofSize: callout?.fontSize ?? 14)], context: nil)
                            titleSize = rect.size
                            calloutRect.size = CGSizeMake(_padding! * 2 + titleSize.width + _borderWidth! * 2, _padding! * 2 + titleSize.height + _borderWidth! * 2 + 5)
                        }
                    }
                    self.frame = calloutRect
                    
                    titleLabel.text = callout?.content
                    titleLabel.font = UIFont.systemFont(ofSize: callout?.fontSize ?? 14)
                    titleLabel.textAlignment = getTextAlignment(textAlign: callout?.textAlign)
                    titleLabel.textColor = callout?.color ?? UIColor.black
                    titleLabel.numberOfLines = 0
                    titleLabel.frame = CGRectMake(_padding! + _borderWidth!, _padding! + _borderWidth!, titleSize.width, titleSize.height)
                    
                    self.isHidden = false
                    self.setNeedsDisplay()
                } else {
                    //如果是空的 就隐藏掉
                    self.isHidden = true
                }
            }
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setup()
    }
    
    init() {
        super.init(frame: CGRectZero)
        setup()
    }
    
    func setup(){
        self.backgroundColor = UIColor.clear
        self.addSubview(self.titleLabel)
    }
    
    func getTextAlignment(textAlign: String?) -> NSTextAlignment {
        switch textAlign {
        case "left":
            return .left
        case "center":
            return .center
        case "right":
            return .right
        default:
            return .left
        }
    }
    
    override func draw(_ rect: CGRect) {
        guard let _ = markerModel else {
            return
        }
        
        guard let context = UIGraphicsGetCurrentContext() else {
            return
        }
        
        let sw = _borderWidth ?? 0 // 线的宽度
        let arrorHeight = CGFloat(5) // 箭头高度
        let minX = sw / 2.0 // 画布最左边x点
        let minY = minX // 画布最上边y点
        let maxWidth = self.bounds.size.width - minX // 画布最右边x点
        let maxHeight = self.bounds.size.height - minX - Double(arrorHeight) - minX // 画布最下边y点
        let midX = self.bounds.midX // 画布中心点
        let r = _borderRadius ?? 0 // 圆角角度
        
        // 画笔线的颜色
        context.setStrokeColor((_borderColor ?? UIColor.clear).cgColor)
        // 设置线宽
        context.setLineWidth(sw)
        // 设置填充颜色
        context.setFillColor((_bgColor ?? UIColor.white).cgColor)
        // 开始落笔从坐标右边开始
        context.move(to: CGPoint(x: maxWidth, y: maxHeight - r))
        
        // 画右下角角度
        context.addArc(tangent1End: CGPoint(x: maxWidth, y: maxHeight), tangent2End: CGPoint(x: maxWidth - r, y: maxHeight), radius: r)
        // 画箭头
        context.addLine(to: CGPoint(x: midX + arrorHeight, y: maxHeight))
        context.addLine(to: CGPoint(x: midX, y: maxHeight + arrorHeight))
        context.addLine(to: CGPoint(x: midX - arrorHeight, y: maxHeight))
        // 画左下角角度
        context.addArc(tangent1End: CGPoint(x: minX, y: maxHeight), tangent2End: CGPoint(x: minX, y: maxHeight - r), radius: r)
        // 画左上角
        context.addArc(tangent1End: CGPoint(x: minX, y: minY), tangent2End: CGPoint(x: maxWidth - r, y: minY), radius: r)
        // 画右上角
        context.addArc(tangent1End: CGPoint(x: maxWidth, y: minY), tangent2End: CGPoint(x: maxWidth, y: maxHeight - r), radius: r)
        // 将最后的笔触和起始点连接起来
        context.closePath()
        // 根据绘制路径并填充颜色
        context.drawPath(using: .fillStroke)
    }
}
