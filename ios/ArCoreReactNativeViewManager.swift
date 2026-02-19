import React
@available(iOS 13.0, *)
@objc(ArCoreReactNativeViewManager)
class ArCoreReactNativeViewManager: RCTViewManager {

    override func view() -> (ArCoreReactNativeView) {
        let view_ = ArCoreReactNativeView()
        view_.setBrigde(brigde: self.bridge)
        return view_
    }

    override static func requiresMainQueueSetup() -> Bool {
        return true
    }

    @objc func addObject(_ node: NSNumber, modelPath: NSString, objectId: NSString) {
        DispatchQueue.main.async {
            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
            viewAr.addObject(name: modelPath as String, idProduct: objectId as String)
        }
    }

    @objc func removeSelectedObject(_ node: NSNumber) {
        DispatchQueue.main.async {
            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
            viewAr.removeObjectSeleted()
        }
    }

    @objc func setDuplicate(_ node: NSNumber) {
        DispatchQueue.main.async {
            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
            viewAr.duplicate = false
        }
    }

    @objc func takeScreenshot(_ node: NSNumber) {
        DispatchQueue.main.async {
            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
            viewAr.takeScreenshot()
        }
    }

    @objc func reset(_ node: NSNumber) {
        DispatchQueue.main.async {
            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
            viewAr.resetSession()
        }
    }
}

import ARKit
import SceneKit
import Photos

@available(iOS 13.0, *)
class ArCoreReactNativeView : UIView {
    var currentGesture: Gesture?
    var screenCenter: CGPoint?
    var dragOnInfinitePlanesEnabled = false
    var use3DOFTrackingFallback = false
    var trackingFallbackTimer: Timer?
    var xPos: Float = 0
    var yPos: Float = 0
    var zPos: Float = 0
    var brigde_: RCTBridge?
    var sceneView: ARSCNView!
    var currentNode: SCNNode?
    var duplicate = false
    var isSend = false
    var sessionConfig = ARWorldTrackingConfiguration()
    var recentVirtualObjectDistances = [CGFloat]()
    let DEFAULT_DISTANCE_CAMERA_TO_OBJECTS = Float(10)

    // MARK: - Event callback props
    @objc var onObjectSelected: RCTDirectEventBlock?
    @objc var onScreenshot: RCTDirectEventBlock?
    @objc var onPlaneDetected: RCTDirectEventBlock?
    @objc var onTrackingStateChange: RCTDirectEventBlock?

    // MARK: - Declarative props
    @objc var planeDetection: NSString = "both" {
        didSet {
            reconfigureSession()
        }
    }

    @objc var lightEstimation: Bool = true {
        didSet {
            reconfigureSession()
        }
    }

    @objc var showPlanes: Bool = false
    @objc var showFeaturePoints: Bool = false {
        didSet {
            if showFeaturePoints {
                sceneView?.debugOptions = [ARSCNDebugOptions.showFeaturePoints, ARSCNDebugOptions.showWorldOrigin]
            } else {
                sceneView?.debugOptions = []
            }
        }
    }

    override init(frame: CGRect) {
        super.init(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height))
        setup()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setup()
    }

    @objc func setBrigde(brigde: RCTBridge) {
        self.brigde_ = brigde
    }

    // MARK: - Session configuration

    func reconfigureSession() {
        let configuration = ARWorldTrackingConfiguration()
        switch planeDetection as String {
        case "horizontal":
            configuration.planeDetection = .horizontal
        case "vertical":
            configuration.planeDetection = .vertical
        case "both":
            configuration.planeDetection = [.horizontal, .vertical]
        case "none":
            configuration.planeDetection = []
        default:
            configuration.planeDetection = [.horizontal, .vertical]
        }
        configuration.isLightEstimationEnabled = lightEstimation
        sceneView?.session.run(configuration)
    }

    func setup() {
        sceneView = ARSCNView()
        self.sceneView.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height)
        DispatchQueue.main.async {
            self.screenCenter = self.sceneView.bounds.mid
        }

        let configuration = ARWorldTrackingConfiguration()
        configuration.planeDetection = [.horizontal, .vertical]
        configuration.isLightEstimationEnabled = lightEstimation

        sceneView.delegate = self
        sceneView.session.delegate = self
        sceneView.session.run(configuration)
        sceneView.autoenablesDefaultLighting = true
        setupFocusSquare()
        self.addSubview(sceneView)
    }

    @objc func removeObjectSeleted() {
        VirtualObjectsManager.shared.removeVirtualObjectSelected()
    }

    func resetSession() {
        sceneView?.session.pause()
        VirtualObjectsManager.shared.resetVirtualObjects()
        let configuration = ARWorldTrackingConfiguration()
        configuration.planeDetection = [.horizontal, .vertical]
        configuration.isLightEstimationEnabled = lightEstimation
        sceneView?.session.run(configuration, options: [.resetTracking, .removeExistingAnchors])
    }

    // MARK: - Event emission helpers

    private func sendObjectSelectedEvent(id: String, selected: Bool) {
        onObjectSelected?(["id": id, "selected": selected])
    }

    private func sendScreenshotEvent(path: String) {
        onScreenshot?(["path": path])
    }

    private func sendPlaneDetectedEvent(id: String, type: String) {
        onPlaneDetected?(["id": id, "type": type])
    }

    private func sendTrackingStateEvent(state: String, reason: String = "") {
        onTrackingStateChange?(["state": state, "reason": reason])
    }

    // MARK: - Touch events
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        let touch = touches.first as! UITouch
        if(touch.view == self.sceneView){
            let viewTouchLocation: CGPoint = touch.location(in: sceneView)
            guard let result = sceneView.hitTest(viewTouchLocation, options: nil).first else {
                self.isSend = false
                VirtualObjectsManager.shared.removeObjectSeletedNull()
                return
            }
            guard let object = VirtualObjectsManager.shared.getNodeEqual(node: result.node) else {
                return
            }
            if !VirtualObjectsManager.shared.equalNode(node: result.node){
                self.isSend = false
            }
            if !isSend {
                sendObjectSelectedEvent(id: object.getName(), selected: true)
                self.isSend = true
            }

            if currentGesture == nil {
                currentGesture = Gesture.startGestureFromTouches(touches, self.sceneView, object)
            } else {
                currentGesture = currentGesture!.updateGestureFromTouches(touches, .touchBegan)
            }

            displayVirtualObjectTransform()
        }
    }

    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        if !VirtualObjectsManager.shared.isAVirtualObjectPlaced() {
            return
        }
        currentGesture = currentGesture?.updateGestureFromTouches(touches, .touchMoved)
        displayVirtualObjectTransform()
    }

    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        if !VirtualObjectsManager.shared.isAVirtualObjectPlaced() {
            return
        }
        currentGesture = currentGesture?.updateGestureFromTouches(touches, .touchEnded)
    }

    override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        if !VirtualObjectsManager.shared.isAVirtualObjectPlaced() {
            return
        }
        currentGesture = currentGesture?.updateGestureFromTouches(touches, .touchCancelled)
    }

    // MARK: - Add object

    func addObject(name: String, idProduct: String) {
        if duplicate {
            return
        }
        let object = VirtualObject(modelName: name, idProduct: idProduct)
        object.viewController = self
        let loaded = object.loadModel()
        if loaded {
            self.showToast(message: "Load object complete!", font: .systemFont(ofSize: 13.0))
        } else {
            self.showToast(message: "Can't load object!", font: .systemFont(ofSize: 13.0))
            return
        }
        VirtualObjectsManager.shared.addVirtualObject(virtualObject: object)
        DispatchQueue.main.async {
            if let lastFocusSquarePos = self.focusSquare?.lastPosition {
                self.setNewVirtualObjectPosition(lastFocusSquarePos)
            } else {
                self.setNewVirtualObjectPosition(SCNVector3Zero)
            }
        }
    }

    // MARK: - Focus Square

    var focusSquare: FocusSquare?
    func setupFocusSquare() {
        focusSquare?.isHidden = true
        focusSquare?.removeFromParentNode()
        focusSquare = FocusSquare()
        sceneView.scene.rootNode.addChildNode(focusSquare!)
    }

    func updateFocusSquare() {
        guard let screenCenter = screenCenter else { return }

        let virtualObject = VirtualObjectsManager.shared.getVirtualObjectSelected()
        if virtualObject != nil && sceneView.isNode(virtualObject!, insideFrustumOf: sceneView.pointOfView!) {
            focusSquare?.hide()
        } else {
            focusSquare?.unhide()
        }

        let (worldPos, planeAnchor, _) = worldPositionFromScreenPosition(screenCenter, objectPos: focusSquare?.position)

        if let worldPos = worldPos {
            focusSquare?.update(for: worldPos, planeAnchor: planeAnchor, camera: sceneView.session.currentFrame?.camera)
        }
    }

    // MARK: - Debug Visualizations
    func refreshFeaturePoints() {
        guard showFeaturePoints else {
            return
        }
        guard sceneView.session.currentFrame?.rawFeaturePoints != nil else {
            return
        }
    }

    var showDebugVisuals: Bool = UserDefaults.standard.bool(for: .debugMode) {
        didSet {
            sceneView.debugOptions = []
            if showDebugVisuals {
                sceneView.debugOptions = [ARSCNDebugOptions.showFeaturePoints, ARSCNDebugOptions.showWorldOrigin]
            }
            UserDefaults.standard.set(showDebugVisuals, for: .debugMode)
        }
    }

    // MARK: - Hit Test Visualization
    var hitTestVisualization: HitTestVisualization?

    var showHitTestAPIVisualization = UserDefaults.standard.bool(for: .showHitTestAPI) {
        didSet {
            if showHitTestAPIVisualization {
                hitTestVisualization = HitTestVisualization(sceneView: sceneView)
            } else {
                hitTestVisualization = nil
            }
        }
    }


    func worldPositionFromScreenPosition(_ position: CGPoint,
                                         objectPos: SCNVector3?,
                                         infinitePlane: Bool = false) -> (position: SCNVector3?,
                                                                          planeAnchor: ARPlaneAnchor?,
                                                                          hitAPlane: Bool) {

        let planeHitTestResults = sceneView.hitTest(position, types: .existingPlane)
        if let result = planeHitTestResults.first {
            let planeHitTestPosition = SCNVector3.positionFromTransform(result.worldTransform)
            xPos = planeHitTestPosition.x
            yPos = planeHitTestPosition.y
            zPos = planeHitTestPosition.z
            let planeAnchor = result.anchor
            return (planeHitTestPosition, planeAnchor as? ARPlaneAnchor, true)
        }

        var featureHitTestPosition: SCNVector3?
        var highQualityFeatureHitTestResult = false

        let highQualityfeatureHitTestResults =
            sceneView.hitTestWithFeatures(position, coneOpeningAngleInDegrees: 18, minDistance: 0.2, maxDistance: 2.0)

        if !highQualityfeatureHitTestResults.isEmpty {
            let result = highQualityfeatureHitTestResults[0]
            featureHitTestPosition = result.position
            highQualityFeatureHitTestResult = true
        }

        if (infinitePlane && dragOnInfinitePlanesEnabled) || !highQualityFeatureHitTestResult {
            let pointOnPlane = objectPos ?? SCNVector3Zero
            let pointOnInfinitePlane = sceneView.hitTestWithInfiniteHorizontalPlane(position, pointOnPlane)
            if pointOnInfinitePlane != nil {
                return (pointOnInfinitePlane, nil, true)
            }
        }

        if highQualityFeatureHitTestResult {
            return (featureHitTestPosition, nil, false)
        }

        let unfilteredFeatureHitTestResults = sceneView.hitTestWithFeatures(position)
        if !unfilteredFeatureHitTestResults.isEmpty {
            let result = unfilteredFeatureHitTestResults[0]
            return (result.position, nil, false)
        }

        return (nil, nil, false)
    }

    func setNewVirtualObjectPosition(_ pos: SCNVector3) {
        guard let object = VirtualObjectsManager.shared.getVirtualObjectSelected(),
              let cameraTransform = sceneView.session.currentFrame?.camera.transform else {
            return
        }

        recentVirtualObjectDistances.removeAll()

        let cameraWorldPos = SCNVector3.positionFromTransform(cameraTransform)
        var cameraToPosition = pos - cameraWorldPos
        cameraToPosition.setMaximumLength(DEFAULT_DISTANCE_CAMERA_TO_OBJECTS)

        object.position = cameraWorldPos + cameraToPosition

        if object.parent == nil {
            sceneView.scene.rootNode.addChildNode(object)
        }
    }

    func resetVirtualObject() {
        VirtualObjectsManager.shared.resetVirtualObjects()
    }

    func updateVirtualObjectPosition(_ pos: SCNVector3, _ filterPosition: Bool) {
        guard let object = VirtualObjectsManager.shared.getVirtualObjectSelected() else {
            return
        }

        guard let cameraTransform = sceneView.session.currentFrame?.camera.transform else {
            return
        }

        let cameraWorldPos = SCNVector3.positionFromTransform(cameraTransform)
        var cameraToPosition = pos - cameraWorldPos
        cameraToPosition.setMaximumLength(DEFAULT_DISTANCE_CAMERA_TO_OBJECTS)

        let hitTestResultDistance = CGFloat(cameraToPosition.length())

        recentVirtualObjectDistances.append(hitTestResultDistance)
        recentVirtualObjectDistances.keepLast(10)

        if filterPosition {
            let averageDistance = recentVirtualObjectDistances.average!

            cameraToPosition.setLength(Float(averageDistance))
            let averagedDistancePos = cameraWorldPos + cameraToPosition

            object.position = averagedDistancePos
        } else {
            object.position = cameraWorldPos + cameraToPosition
        }
    }

    func checkIfObjectShouldMoveOntoPlane(anchor: ARPlaneAnchor) {
        guard let object = VirtualObjectsManager.shared.getVirtualObjectSelected(),
            let planeAnchorNode = sceneView.node(for: anchor) else {
            return
        }

        let objectPos = planeAnchorNode.convertPosition(object.position, from: object.parent)

        if objectPos.y == 0 {
            return
        }

        let tolerance: Float = 0.1

        let minX: Float = anchor.center.x - anchor.extent.x / 2 - anchor.extent.x * tolerance
        let maxX: Float = anchor.center.x + anchor.extent.x / 2 + anchor.extent.x * tolerance
        let minZ: Float = anchor.center.z - anchor.extent.z / 2 - anchor.extent.z * tolerance
        let maxZ: Float = anchor.center.z + anchor.extent.z / 2 + anchor.extent.z * tolerance

        if objectPos.x < minX || objectPos.x > maxX || objectPos.z < minZ || objectPos.z > maxZ {
            return
        }

        let verticalAllowance: Float = 0.03
        if objectPos.y > -verticalAllowance && objectPos.y < verticalAllowance {
            SCNTransaction.begin()
            SCNTransaction.animationDuration = 0.5
            SCNTransaction.animationTimingFunction = CAMediaTimingFunction(name: CAMediaTimingFunctionName.easeInEaseOut)
            object.position.y = anchor.transform.columns.3.y
            SCNTransaction.commit()
        }
    }

    // MARK: - Planes

    var planes = [ARPlaneAnchor: Plane]()

    func addPlane(node: SCNNode, anchor: ARPlaneAnchor) {
        let plane = Plane(anchor)
        planes[anchor] = plane
        node.addChildNode(plane)

        let type = anchor.alignment == .horizontal ? "horizontal" : "vertical"
        sendPlaneDetectedEvent(id: anchor.identifier.uuidString, type: type)

        if !VirtualObjectsManager.shared.isAVirtualObjectPlaced() {
            // Surface detected, ready for object placement
        }
    }
}


// MARK: - ARSessionDelegate
@available(iOS 13.0, *)
extension ArCoreReactNativeView: ARSessionDelegate {
    func setupScene() {
        sceneView.delegate = self
        sceneView.session = sceneView.session
        sceneView.antialiasingMode = .multisampling4X
        sceneView.automaticallyUpdatesLighting = true
        sceneView.preferredFramesPerSecond = 60
        sceneView.contentScaleFactor = 1.3
        sceneView.enableEnvironmentMapWithIntensity(25.0)
    }

    func session(_ session: ARSession, cameraDidChangeTrackingState camera: ARCamera) {
        switch camera.trackingState {
        case .notAvailable:
            sendTrackingStateEvent(state: "notAvailable")
        case .limited(let reason):
            var reasonStr = ""
            switch reason {
            case .excessiveMotion:
                reasonStr = "excessiveMotion"
            case .insufficientFeatures:
                reasonStr = "insufficientFeatures"
            case .initializing:
                reasonStr = "initializing"
            case .relocalizing:
                reasonStr = "relocalizing"
            @unknown default:
                reasonStr = "unknown"
            }
            sendTrackingStateEvent(state: "limited", reason: reasonStr)

            if use3DOFTrackingFallback {
                trackingFallbackTimer = Timer.scheduledTimer(withTimeInterval: 10.0, repeats: false, block: { _ in
                    self.trackingFallbackTimer?.invalidate()
                    self.trackingFallbackTimer = nil
                })
            }
        case .normal:
            sendTrackingStateEvent(state: "normal")
            if use3DOFTrackingFallback && trackingFallbackTimer != nil {
                trackingFallbackTimer!.invalidate()
                trackingFallbackTimer = nil
            }
        }
    }

    func session(_ session: ARSession, didFailWithError error: Error) {
        guard let arError = error as? ARError else { return }

        let nsError = error as NSError
        var sessionErrorMsg = "\(nsError.localizedDescription) \(nsError.localizedFailureReason ?? "")"
        if let recoveryOptions = nsError.localizedRecoveryOptions {
            for option in recoveryOptions {
                sessionErrorMsg.append("\(option).")
            }
        }

        let isRecoverable = (arError.code == .worldTrackingFailed)
        if isRecoverable {
            sessionErrorMsg += "\nYou can try resetting the session or quit the application."
        } else {
            sessionErrorMsg += "\nThis is an unrecoverable error that requires to quit the application."
        }
    }

    func sessionWasInterrupted(_ session: ARSession) {
        // Session was interrupted
    }

    func sessionInterruptionEnded(_ session: ARSession) {
        session.run(sessionConfig, options: [.resetTracking, .removeExistingAnchors])
    }
}


// MARK: - ARSCNViewDelegate
@available(iOS 13.0, *)
extension ArCoreReactNativeView: ARSCNViewDelegate {
    func renderer(_ renderer: SCNSceneRenderer, updateAtTime time: TimeInterval) {
        refreshFeaturePoints()

        DispatchQueue.main.async {
            self.updateFocusSquare()
            self.hitTestVisualization?.render()

            if let lightEstimate = self.sceneView.session.currentFrame?.lightEstimate {
                self.sceneView.enableEnvironmentMapWithIntensity(lightEstimate.ambientIntensity / 40)
            } else {
                self.sceneView.enableEnvironmentMapWithIntensity(25)
            }
        }
    }

    func renderer(_ renderer: SCNSceneRenderer, didAdd node: SCNNode, for anchor: ARAnchor) {
        DispatchQueue.main.async {
            if let planeAnchor = anchor as? ARPlaneAnchor {
                self.addPlane(node: node, anchor: planeAnchor)
                self.checkIfObjectShouldMoveOntoPlane(anchor: planeAnchor)
            }
        }
    }

    func renderer(_ renderer: SCNSceneRenderer, didUpdate node: SCNNode, for anchor: ARAnchor) {
        DispatchQueue.main.async {
            if let planeAnchor = anchor as? ARPlaneAnchor {
                if let plane = self.planes[planeAnchor] {
                    plane.update(planeAnchor)
                }
                self.checkIfObjectShouldMoveOntoPlane(anchor: planeAnchor)
            }
        }
    }

    func renderer(_ renderer: SCNSceneRenderer, didRemove node: SCNNode, for anchor: ARAnchor) {
        DispatchQueue.main.async {
            if let planeAnchor = anchor as? ARPlaneAnchor {
                self.planes.removeValue(forKey: planeAnchor)
            }
        }
    }
}


// MARK: - Display View
@available(iOS 13.0, *)
extension ArCoreReactNativeView {
    func displayVirtualObjectTransform() {
        guard let object = VirtualObjectsManager.shared.getVirtualObjectSelected(),
              let cameraTransform = self.sceneView.session.currentFrame?.camera.transform else {
            return
        }

        let cameraPos = SCNVector3.positionFromTransform(cameraTransform)
        let vectorToCamera = cameraPos - object.position
        let distanceToUser = vectorToCamera.length()

        var angleDegrees = Int(((object.eulerAngles.y) * 180) / Float.pi) % 360
        if angleDegrees < 0 {
            angleDegrees += 360
        }
    }

    func moveVirtualObjectToPosition(_ pos: SCNVector3?, _ instantly: Bool, _ filterPosition: Bool) {
        guard let newPosition = pos else {
            if !VirtualObjectsManager.shared.isAVirtualObjectPlaced() {
                resetVirtualObject()
            }
            return
        }

        if instantly {
            setNewVirtualObjectPosition(newPosition)
        } else {
            updateVirtualObjectPosition(newPosition, filterPosition)
        }
    }

    func showToast(message: String, font: UIFont) {
        let toastLabel = UILabel(frame: CGRect(x: UIScreen.main.bounds.width/2 - 75, y: UIScreen.main.bounds.height-100, width: 150, height: 35))
        toastLabel.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        toastLabel.textColor = UIColor.white
        toastLabel.font = font
        toastLabel.textAlignment = .center
        toastLabel.text = message
        toastLabel.alpha = 1.0
        toastLabel.layer.cornerRadius = 10
        toastLabel.clipsToBounds = true
        self.sceneView.addSubview(toastLabel)
        UIView.animate(withDuration: 4.0, delay: 0.1, options: .curveEaseOut, animations: {
             toastLabel.alpha = 0.0
        }, completion: {(isCompleted) in
            toastLabel.removeFromSuperview()
        })
    }

    @objc func takeScreenshot() -> UIImage {
        UIGraphicsBeginImageContextWithOptions(self.bounds.size, false, UIScreen.main.scale)
        drawHierarchy(in: self.bounds, afterScreenUpdates: true)
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        if let image = image {
            let saved = saveImage(image: image)
            return image
        }
        sendScreenshotEvent(path: "")
        return UIImage()
    }

    func saveImage(image: UIImage) -> Bool {
        guard let data = image.jpegData(compressionQuality: 1) ?? image.pngData() else {
            return false
        }
        guard let directory = try? FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false) as NSURL else {
            return false
        }
        do {
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM-dd HH:mm:ss "
            let now = Date()
            let dateString = formatter.string(from: now)
            let fileUrl = directory.appendingPathComponent(dateString + "avr.png")
            try data.write(to: fileUrl!)
            sendScreenshotEvent(path: fileUrl?.path ?? "")
            return true
        } catch {
            print(error.localizedDescription)
            sendScreenshotEvent(path: "")
            return false
        }
    }
}
