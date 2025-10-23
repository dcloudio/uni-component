//
//  SpinnerView.swift
//  SpinnerView
//
//  Created by Fred on 2025/9/12.
//

import UIKit

public class SpinnerView: UIView {
    private let foregroundLayer = CAShapeLayer()
    private var isAnimating = false
    private var configuredLineWidth: CGFloat = 2
    private var configuredStrokeColor: UIColor = .black
    private let spinner_key: String = "spinner.rotation"
    private var configuredRotationDuration: CFTimeInterval = 1.333
    private var arcLength: CGFloat = 0.75
    private var animationTimingFunction: CAMediaTimingFunction = CAMediaTimingFunction(name: .linear)

    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        commonInit()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        commonInit()
    }

    private func commonInit() {
        translatesAutoresizingMaskIntoConstraints = false

        if frame == .zero {
            self.frame = CGRect(x: 0, y: 0, width: 16, height: 16)
        }

        foregroundLayer.strokeColor = configuredStrokeColor.cgColor
        foregroundLayer.fillColor = UIColor.clear.cgColor
        foregroundLayer.lineWidth = configuredLineWidth
        foregroundLayer.lineCap = .butt
        foregroundLayer.lineJoin = .round

        layer.addSublayer(foregroundLayer)
        installAppLifecycleObservers()
    }

    public override func layoutSubviews() {
        super.layoutSubviews()
        updatePath()
    }
    
    private func updatePath() {
        let size = min(bounds.width, bounds.height)
        let radius = (size - configuredLineWidth) / 2
        let center = CGPoint(x: bounds.midX, y: bounds.midY)

        let startAngle: CGFloat = -.pi / 2
        let endAngle: CGFloat = startAngle + 2 * .pi * arcLength
        let foregroundPath = UIBezierPath(
            arcCenter: center, radius: radius, startAngle: startAngle, endAngle: endAngle,
            clockwise: true)

        foregroundLayer.path = foregroundPath.cgPath
        foregroundLayer.frame = bounds
    }

    func start() {
        guard !isAnimating else { return }
        isAnimating = true

        foregroundLayer.speed = 1
        foregroundLayer.timeOffset = 0
        foregroundLayer.beginTime = 0
        applyRotationAnimation(preserveProgress: false)
    }

    func stop() {
        guard isAnimating else { return }
        isAnimating = false
        foregroundLayer.removeAnimation(forKey: spinner_key)
    }

    func setAppearance(strokeColor: UIColor, lineWidth: CGFloat) {
        setAppearance(
            strokeColor: strokeColor,
            lineWidth: lineWidth,
            strokeBGColor: nil,
            rotationDuration: nil,
            animating: nil,
            animationTimingFunction: nil)
    }

    func setAppearance(strokeColor: UIColor, lineWidth: CGFloat, animationTimingFunction: String? = nil) {
        setAppearance(
            strokeColor: strokeColor,
            lineWidth: lineWidth,
            strokeBGColor: nil,
            rotationDuration: nil,
            animating: nil,
            animationTimingFunction: animationTimingFunction)
    }

    public func setAppearance(
        strokeColor: UIColor,
        lineWidth: CGFloat,
        strokeBGColor: UIColor? = nil,
        rotationDuration: CFTimeInterval? = nil,
        animating: Bool? = nil,
        animationTimingFunction: String? = nil
    ) {
        configuredStrokeColor = strokeColor
        configuredLineWidth = lineWidth
        if let rotationDuration = rotationDuration {
            configuredRotationDuration = max(0.1, rotationDuration)
        }
        
        if let animationTimingFunction = animationTimingFunction, animationTimingFunction.lowercased() == "ease" {
            self.animationTimingFunction = CAMediaTimingFunction(name: .easeInEaseOut)
        }

        foregroundLayer.strokeColor = strokeColor.cgColor
        foregroundLayer.lineWidth = lineWidth

        updatePath()

        if let animating = animating {
            if animating { start() } else { stop() }
        } else if isAnimating, rotationDuration != nil {
            applyRotationAnimation(preserveProgress: true)
        }
    }
    
    func setArcLength(_ length: CGFloat) {
        arcLength = max(0.1, min(1.0, length))
        updatePath()
    }
    
    func setRotationSpeed(_ speed: CGFloat) {
        configuredRotationDuration = CFTimeInterval(1.0 / speed)
        if isAnimating {
            applyRotationAnimation(preserveProgress: true)
        }
    }

    private func installAppLifecycleObservers() {
        NotificationCenter.default.addObserver(
            self, selector: #selector(handleDidBecomeActive),
            name: UIApplication.didBecomeActiveNotification, object: nil)
        NotificationCenter.default.addObserver(
            self, selector: #selector(handleDidEnterBackground),
            name: UIApplication.didEnterBackgroundNotification, object: nil)
        NotificationCenter.default.addObserver(
            self, selector: #selector(handleWillEnterForeground),
            name: UIApplication.willEnterForegroundNotification, object: nil)
    }

    @objc private func handleDidEnterBackground() {
        guard isAnimating else { return }
        pauseLayer(foregroundLayer)
    }

    @objc private func handleWillEnterForeground() {
        guard isAnimating else { return }
        resumeLayerIfPaused(foregroundLayer)
    }

    @objc private func handleDidBecomeActive() {
        guard isAnimating else { return }
        if foregroundLayer.animation(forKey: spinner_key) == nil {
            start()
            return
        }
        resumeLayerIfPaused(foregroundLayer)
    }

    private func pauseLayer(_ layer: CALayer) {
        guard layer.speed != 0 else { return }
        let pausedTime = layer.convertTime(CACurrentMediaTime(), from: nil)
        layer.speed = 0
        layer.timeOffset = pausedTime
    }

    private func resumeLayerIfPaused(_ layer: CALayer) {
        guard layer.speed == 0 else { return }
        let pausedTime = layer.timeOffset
        layer.speed = 1
        layer.timeOffset = 0
        layer.beginTime = 0
        let timeSincePause = layer.convertTime(CACurrentMediaTime(), from: nil) - pausedTime
        layer.beginTime = timeSincePause
    }

    private func applyRotationAnimation(preserveProgress: Bool) {
        let currentAngle: CGFloat
        if preserveProgress, let pres = foregroundLayer.presentation(),
            let angle = pres.value(forKeyPath: "transform.rotation.z") as? CGFloat
        {
            currentAngle = angle
        } else {
            currentAngle = 0
        }

        foregroundLayer.removeAnimation(forKey: spinner_key)

        let rotationAnimation = CABasicAnimation(keyPath: "transform.rotation.z")
        rotationAnimation.fromValue = currentAngle
        rotationAnimation.toValue = currentAngle + 2 * CGFloat.pi
        rotationAnimation.duration = configuredRotationDuration
        rotationAnimation.repeatCount = .infinity
        rotationAnimation.timingFunction = self.animationTimingFunction
        rotationAnimation.isRemovedOnCompletion = false
        foregroundLayer.add(rotationAnimation, forKey: spinner_key)
    }
}
