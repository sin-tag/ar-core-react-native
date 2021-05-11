//
//  SceneView+Extension.swift
//  ar-core-react-native
//
//  // create by hoangtuyensk@gmail.com - github: sig-tag
//

import ARKit
import Foundation
import SceneKit

@available(iOS 11.0, *)
extension ARSCNView {
    func setUp(viewController: ArCoreReactNativeView, session: ARSession) {
        self.delegate = viewController as! ARSCNViewDelegate
        self.session = session
        self.antialiasingMode = .multisampling4X
        self.automaticallyUpdatesLighting = true
        self.preferredFramesPerSecond = 60
        self.contentScaleFactor = 1.3
        self.enableEnvironmentMapWithIntensity(25.0)
        if let camera = pointOfView?.camera {
            camera.wantsHDR = true
            camera.wantsExposureAdaptation = true
            camera.exposureOffset = -1
            camera.minimumExposure = -1
        }
    }

    func enableEnvironmentMapWithIntensity(_ intensity: CGFloat) {
        if scene.lightingEnvironment.contents == nil {
            if let environmentMap = UIImage(named: "Models.scnassets/sharedImages/environment_blur.exr") {
                scene.lightingEnvironment.contents = environmentMap
            }
        }
        scene.lightingEnvironment.intensity = intensity
    }
}
