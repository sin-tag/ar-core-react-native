// create by hoangtuyensk@gmail.com - github: sig-tag
import React
@available(iOS 11.0, *)
@objc(ArCoreReactNativeViewManager)
class ArCoreReactNativeViewManager: RCTViewManager {

//    @available(iOS 11.0, *)
    override func view() -> (ArCoreReactNativeView) {
        let view_ = ArCoreReactNativeView()
        view_.setBrigde(brigde: self.bridge)
        return view_
    }
//    @objc func testAction(_ node:NSNumber, parameterName nameObject:NSString){
//        DispatchQueue.main.async {
//            print("run testAction")
//            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
//            viewAr.usingBrigde()
//        }
//    }
    @objc func CMD_RUN_SET_OBJECT(_ node:NSNumber, parameterName nameObject: NSString, parameterID idProduct:NSString){
        DispatchQueue.main.async {
            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
            viewAr.addObject(name: nameObject as String, idProduct:idProduct as String)
        }
    }
    @objc func CMD_RUN_DELETE_OBJECT(_ node:NSNumber){
        DispatchQueue.main.async {
            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
            viewAr.removeObjectSeleted()
          }
    }
    @objc func CMD_RUN_SET_DUPLICATE(_ node:NSNumber){
        DispatchQueue.main.async {
            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
          }
    }
    @objc func CMD_RUN_GET_ID(_ node:NSNumber){
        DispatchQueue.main.async {
            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
          }
    }
    @objc func CMD_RUN_KILL_PROCESS(_ node:NSNumber){
        DispatchQueue.main.async {
            let viewAr = self.bridge.uiManager.view(forReactTag: node) as! ArCoreReactNativeView
          }
    }
}

import ARKit
import SceneKit
import Photos
@available(iOS 11.0, *)
class ArCoreReactNativeView : UIView {
    var currentGesture: Gesture?
    var screenCenter: CGPoint?
    var dragOnInfinitePlanesEnabled = false
    var use3DOFTrackingFallback = false
    var trackingFallbackTimer: Timer?
    var xPos:Float = 0;
    var yPos:Float = 0;
    var zPos: Float = 0;
    var brigde_ : RCTBridge?;
    var sceneView: ARSCNView!;
    var currentNode: SCNNode?
    var sessionConfig = ARWorldTrackingConfiguration();
    var recentVirtualObjectDistances = [CGFloat]()
    let DEFAULT_DISTANCE_CAMERA_TO_OBJECTS = Float(10)
    
    override init(frame: CGRect) {
        super.init(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height));
        setup();
        
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder);
        setup();
    }
    @ objc func setBrigde(brigde: RCTBridge){
        self.brigde_ = brigde
    }
    
    func setup() {
        sceneView = ARSCNView();
        self.sceneView.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height);
        DispatchQueue.main.async {
            self.screenCenter = self.sceneView.bounds.mid
        }
//        sceneView.debugOptions = [ARSCNDebugOptions.showFeaturePoints,ARSCNDebugOptions.showWorldOrigin]
        let configuration = ARWorldTrackingConfiguration()
        if #available(iOS 11.3, *){
            configuration.planeDetection = [.horizontal, .vertical]
        }
        else{
            configuration.planeDetection = .horizontal
        }
        
        configuration.isLightEstimationEnabled = UserDefaults.standard.bool(for: .ambientLightEstimation)
        sceneView.delegate = self
        sceneView.session.run(configuration)
        setupFocusSquare();
        self.addSubview(sceneView)
    }
    
    @objc func removeObjectSeleted() {
        VirtualObjectsManager.shared.removeVirtualObjectSelected()
    }
        override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
            super.touchesBegan(touches, with: event)
            guard let object = VirtualObjectsManager.shared.getVirtualObjectSelected() else {
                return
            }
            if currentGesture == nil {
                currentGesture = Gesture.startGestureFromTouches(touches, self.sceneView, object)
            } else {
                currentGesture = currentGesture!.updateGestureFromTouches(touches, .touchBegan)
            }
    
            displayVirtualObjectTransform()
        }
    @objc func moveNode(_ gesture: UIPanGestureRecognizer) {

    //1. Get The Current Touch Point
    let currentTouchPoint = gesture.location(in: self.sceneView)

    //2. If The Gesture State Has Begun Perform A Hit Test To Get The SCNNode At The Touch Location
    if gesture.state == .began{

        //2a. Perform An SCNHitTest To Detect If An SCNNode Has Been Touched
        guard let nodeHitTest = self.sceneView.hitTest(currentTouchPoint, options: nil).first else { return }

        //2b. Get The SCNNode Result
        let nodeHit = nodeHitTest.node

        //2c. Set As The Current Node
        currentNode = nodeHit

    }
    //3. If The Gesture State Has Changed Then Perform An ARSCNHitTest To Detect Any Existing Planes
    if gesture.state == .changed{

        //3b. Get The Next Feature Point Etc
        guard let hitTest = self.sceneView.hitTest(currentTouchPoint, types: .existingPlane).first else { return }

        //3c. Convert To World Coordinates
        let worldTransform = hitTest.worldTransform

        //3d. Set The New Position
        let newPosition = SCNVector3(worldTransform.columns.3.x, worldTransform.columns.3.y, worldTransform.columns.3.z)

        //3e. Apply To The Node
        currentNode?.simdPosition = float3(newPosition.x, newPosition.y, newPosition.z)

    }

    //4. If The Gesture State Has Ended Remove The Reference To The Current Node
    if gesture.state == .ended{
        currentNode = nil
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
            let touch_ = touches.first!
    //        print(touch_.location(in: self.view))
            if !VirtualObjectsManager.shared.isAVirtualObjectPlaced() {
                let allObjects = VirtualObjectsManager.shared.getAllObjects()
                for object in allObjects{
                }
    //            chooseObject(addObjectButton)
                return
            }
    
            currentGesture = currentGesture?.updateGestureFromTouches(touches, .touchEnded)
//            self.bridge.eventDispatcher()?.sendAppEvent(withName: "EMIT_GET_TEST", body: "send nef")
        }
    
        override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
            if !VirtualObjectsManager.shared.isAVirtualObjectPlaced() {
                return
            }
            currentGesture = currentGesture?.updateGestureFromTouches(touches, .touchCancelled)
        }
    
    
    // MARK: - Add obj
    
    func addObject(name: String, idProduct: String) {
        let object = VirtualObject(modelName: name, idProduct: idProduct);
        object.viewController = self
        let loaded = object.loadModel()
        if (loaded){
            self.showToast(message: "Load object comlete!", font: .systemFont(ofSize: 13.0))
            
        }
        else{
            self.showToast(message: "Can't load object!", font: .systemFont(ofSize: 13.0))
            return
        }
        VirtualObjectsManager.shared.addVirtualObject(virtualObject: object)
        let value = ["ID": object.getName()]
        self.brigde_?.eventDispatcher()?.sendAppEvent(withName: "EMIT_GET_NAME", body:value )
        DispatchQueue.main.async {
            if let lastFocusSquarePos = self.focusSquare?.lastPosition {
                self.setNewVirtualObjectPosition(lastFocusSquarePos)
            } else {
                self.setNewVirtualObjectPosition(SCNVector3Zero)
            }
        }
        
        
    }
    
    
    
    // MARK: - Focus Square

    var focusSquare: FocusSquare?;
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
        
//        print("screen center: ", screenCenter)
        if let worldPos = worldPos {
            focusSquare?.update(for: worldPos, planeAnchor: planeAnchor, camera: sceneView.session.currentFrame?.camera)
//            textManager.cancelScheduledMessage(forType: .focusSquare)
//            print("update focus square")
        }
    }
    
    // MARK: - Debug Visualizations
    func refreshFeaturePoints() {
        guard showDebugVisuals else {
            return
        }

        guard let cloud = sceneView.session.currentFrame?.rawFeaturePoints else {
            return
        }

        DispatchQueue.main.async {
//            self.featurePointCountLabel.text = "Features: \(cloud.__count)".uppercased()
        }
    }

    var showDebugVisuals: Bool = UserDefaults.standard.bool(for: .debugMode) {
        didSet {
//            featurePointCountLabel.isHidden = !showDebugVisuals
//            debugMessageLabel.isHidden = !showDebugVisuals
//            messagePanel.isHidden = !showDebugVisuals
//            planes.values.forEach { $0.showDebugVisualization(showDebugVisuals) }
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
//            UserDefaults.standard.set(showHitTestAPIVisualization, for: .showHitTestAPI)
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

        // -------------------------------------------------------------------------------
        // 1. Always do a hit test against exisiting plane anchors first.
        //    (If any such anchors exist & only within their extents.)

        let planeHitTestResults = sceneView.hitTest(position, types: .existingPlaneUsingExtent)
//        print("plan:",planeHitTestResults)
        if let result = planeHitTestResults.first {
            let planeHitTestPosition = SCNVector3.positionFromTransform(result.worldTransform)
            xPos = planeHitTestPosition.x
            yPos = planeHitTestPosition.y
            zPos = planeHitTestPosition.z
            let planeAnchor = result.anchor

            // Return immediately - this is the best possible outcome.
            return (planeHitTestPosition, planeAnchor as? ARPlaneAnchor, true)
        }
        

        // -------------------------------------------------------------------------------
        // 2. Collect more information about the environment by hit testing against
        //    the feature point cloud, but do not return the result yet.
        var featureHitTestPosition: SCNVector3?
        var highQualityFeatureHitTestResult = false

        let highQualityfeatureHitTestResults =
            sceneView.hitTestWithFeatures(position, coneOpeningAngleInDegrees: 18, minDistance: 0.2, maxDistance: 2.0)

        if !highQualityfeatureHitTestResults.isEmpty {
            let result = highQualityfeatureHitTestResults[0]
            featureHitTestPosition = result.position
            highQualityFeatureHitTestResult = true
        }

        // -------------------------------------------------------------------------------
        // 3. If desired or necessary (no good feature hit test result): Hit test
        //    against an infinite, horizontal plane (ignoring the real world).

        if (infinitePlane && dragOnInfinitePlanesEnabled) || !highQualityFeatureHitTestResult {

            let pointOnPlane = objectPos ?? SCNVector3Zero

            let pointOnInfinitePlane = sceneView.hitTestWithInfiniteHorizontalPlane(position, pointOnPlane)
            if pointOnInfinitePlane != nil {
                return (pointOnInfinitePlane, nil, true)
            }
        }

        // -------------------------------------------------------------------------------
        // 4. If available, return the result of the hit test against high quality
        //    features if the hit tests against infinite planes were skipped or no
        //    infinite plane was hit.

        if highQualityFeatureHitTestResult {
            return (featureHitTestPosition, nil, false)
        }

        // -------------------------------------------------------------------------------
        // 5. As a last resort, perform a second, unfiltered hit test against features.
        //    If there are no features in the scene, the result returned here will be nil.

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

//        addObjectButton.setImage(#imageLiteral(resourceName: "add"), for: [])
//        addObjectButton.setImage(#imageLiteral(resourceName: "addPressed"), for: [.highlighted])
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

        // Compute the average distance of the object from the camera over the last ten
        // updates. If filterPosition is true, compute a new position for the object
        // with this average. Notice that the distance is applied to the vector from
        // the camera to the content, so it only affects the percieved distance of the
        // object - the averaging does _not_ make the content "lag".
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

        // Get the object's position in the plane's coordinate system.
        let objectPos = planeAnchorNode.convertPosition(object.position, from: object.parent)

        if objectPos.y == 0 {
            return; // The object is already on the plane
        }

        // Add 10% tolerance to the corners of the plane.
        let tolerance: Float = 0.1

        let minX: Float = anchor.center.x - anchor.extent.x / 2 - anchor.extent.x * tolerance
        let maxX: Float = anchor.center.x + anchor.extent.x / 2 + anchor.extent.x * tolerance
        let minZ: Float = anchor.center.z - anchor.extent.z / 2 - anchor.extent.z * tolerance
        let maxZ: Float = anchor.center.z + anchor.extent.z / 2 + anchor.extent.z * tolerance

        if objectPos.x < minX || objectPos.x > maxX || objectPos.z < minZ || objectPos.z > maxZ {
            return
        }

        // Drop the object onto the plane if it is near it.
        let verticalAllowance: Float = 0.03
        if objectPos.y > -verticalAllowance && objectPos.y < verticalAllowance {
//            textManager.showDebugMessage("OBJECT MOVED\nSurface detected nearby")

            SCNTransaction.begin()
            SCNTransaction.animationDuration = 0.5
            SCNTransaction.animationTimingFunction = CAMediaTimingFunction(name: CAMediaTimingFunctionName.easeInEaseOut)
            object.position.y = anchor.transform.columns.3.y
            SCNTransaction.commit()
        }
    }
    
    
    
    // MARK: Gesture Recognized
    

    
    // MARK: - Planes

    var planes = [ARPlaneAnchor: Plane]()

    func addPlane(node: SCNNode, anchor: ARPlaneAnchor) {

        let pos = SCNVector3.positionFromTransform(anchor.transform)
//        textManager.showDebugMessage("NEW SURFACE DETECTED AT \(pos.friendlyString())")

        let plane = Plane(anchor)

        planes[anchor] = plane
        node.addChildNode(plane)

//        textManager.cancelScheduledMessage(forType: .planeEstimation)
//        textManager.showMessage("SURFACE DETECTED")
        if !VirtualObjectsManager.shared.isAVirtualObjectPlaced() {
//            textManager.scheduleMessage("TAP + TO PLACE AN OBJECT", inSeconds: 7.5, messageType: .contentPlacement)
        }
    }

  @objc var color: String = "" {
    didSet {
      self.backgroundColor = hexStringToUIColor(hexColor: color)
    }
  }

  func hexStringToUIColor(hexColor: String) -> UIColor {
    let stringScanner = Scanner(string: hexColor)

    if(hexColor.hasPrefix("#")) {
      stringScanner.scanLocation = 1
    }
    var color: UInt32 = 0
    stringScanner.scanHexInt32(&color)

    let r = CGFloat(Int(color >> 16) & 0x000000FF)
    let g = CGFloat(Int(color >> 8) & 0x000000FF)
    let b = CGFloat(Int(color) & 0x000000FF)

    return UIColor(red: 255.0 / 255.0, green: 255.0 / 255.0, blue: 255.0 / 255.0, alpha: 1)
  }
}


@available(iOS 11.0, *)
extension ArCoreReactNativeView: ARSessionDelegate {
    func setupScene() {
//        sceneView.setUp(viewController: self, session: session)
        sceneView.delegate = self
        sceneView.session = sceneView.session
        sceneView.antialiasingMode = .multisampling4X
        sceneView.automaticallyUpdatesLighting = true
        sceneView.preferredFramesPerSecond = 60
        sceneView.contentScaleFactor = 1.3
        sceneView.enableEnvironmentMapWithIntensity(25.0)
//        if let camera = pointOfView?.camera {
//            camera.wantsHDR = true
//            camera.wantsExposureAdaptation = true
//            camera.exposureOffset = -1
//            camera.minimumExposure = -1
//        }
        
    }
    
    func session(_ session: ARSession, cameraDidChangeTrackingState camera: ARCamera) {
//        textManager.showTrackingQualityInfo(for: camera.trackingState, autoHide: !self.showDebugVisuals)

        switch camera.trackingState {
        case .notAvailable: break
//            textManager.escalateFeedback(for: camera.trackingState, inSeconds: 5.0)
        case .limited:
            if use3DOFTrackingFallback {
                // After 10 seconds of limited quality, fall back to 3DOF mode.
                trackingFallbackTimer = Timer.scheduledTimer(withTimeInterval: 10.0, repeats: false, block: { _ in
                    self.trackingFallbackTimer?.invalidate()
                    self.trackingFallbackTimer = nil
                })
            } else {
//                textManager.escalateFeedback(for: camera.trackingState, inSeconds: 10.0)
            }
        case .normal:
//            textManager.cancelScheduledMessage(forType: .trackingStateEscalation)
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

//        displayErrorMessage(title: "We're sorry!", message: sessionErrorMsg, allowRestart: isRecoverable)
    }

    func sessionWasInterrupted(_ session: ARSession) {
//        textManager.blurBackground()
//        textManager.showAlert(title: "Session Interrupted",
//                              message: "The session will be reset after the interruption has ended.")
    }

    func sessionInterruptionEnded(_ session: ARSession) {
//        textManager.unblurBackground()
        session.run(sessionConfig, options: [.resetTracking, .removeExistingAnchors])
//        restartExperience(self)
//        textManager.showMessage("RESETTING SESSION")
    }
}


// MARK: - ARSCNViewDelegate
@available(iOS 11.0, *)
extension ArCoreReactNativeView: ARSCNViewDelegate {
    func renderer(_ renderer: SCNSceneRenderer, updateAtTime time: TimeInterval) {
        refreshFeaturePoints()

        DispatchQueue.main.async {
            self.updateFocusSquare()
            self.hitTestVisualization?.render()

            // If light estimation is enabled, update the intensity of the model's lights and the environment map
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
//            }
        }
    }

    func renderer(_ renderer: SCNSceneRenderer, didRemove node: SCNNode, for anchor: ARAnchor) {
        DispatchQueue.main.async {
//            if let planeAnchor = anchor as? ARPlaneAnchor, let plane = self.planes.removeValue(forKey: planeAnchor) {
//                plane.removeFromParentNode()
//            }
        }
    }
}

}




// MARK: - Display View
@available(iOS 11.0, *)
extension ArCoreReactNativeView {
    func displayVirtualObjectTransform() {
        guard let object = VirtualObjectsManager.shared.getVirtualObjectSelected(),
              let cameraTransform = self.sceneView.session.currentFrame?.camera.transform else {
            return
        }
        
        // Output the current translation, rotation & scale of the virtual object as text.
        let cameraPos = SCNVector3.positionFromTransform(cameraTransform)
        
        let vectorToCamera = cameraPos - object.position

        let distanceToUser = vectorToCamera.length()

        var angleDegrees = Int(((object.eulerAngles.y) * 180) / Float.pi) % 360
        if angleDegrees < 0 {
            angleDegrees += 360
        }

        let distance = String(format: "%.2f", distanceToUser)
        let scale = String(format: "%.2f", object.scale.x)
//        textManager.showDebugMessage("Distance: \(distance) m\nRotation: \(angleDegrees)°\nScale: \(scale)x")
    }

    func moveVirtualObjectToPosition(_ pos: SCNVector3?, _ instantly: Bool, _ filterPosition: Bool) {

        guard let newPosition = pos else {
//            textManager.showMessage("CANNOT PLACE OBJECT\nTry moving left or right.")
            // Reset the content selection in the menu only if the content has not yet been initially placed.
            if !VirtualObjectsManager.shared.isAVirtualObjectPlaced() {
                resetVirtualObject()
            }
            return
        }
        print("newPosition:", pos)

        if instantly {
            setNewVirtualObjectPosition(newPosition)
        } else {
            updateVirtualObjectPosition(newPosition, filterPosition)
        }
    }
    func showToast(message : String, font: UIFont) {

        let toastLabel = UILabel(frame: CGRect(x: UIScreen.main.bounds.width/2 - 75, y: UIScreen.main.bounds.height-100, width: 150, height: 35))
        toastLabel.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        toastLabel.textColor = UIColor.white
        toastLabel.font = font
        toastLabel.textAlignment = .center;
        toastLabel.text = message
        toastLabel.alpha = 1.0
        toastLabel.layer.cornerRadius = 10;
        toastLabel.clipsToBounds  =  true
        self.sceneView.addSubview(toastLabel)
        UIView.animate(withDuration: 4.0, delay: 0.1, options: .curveEaseOut, animations: {
             toastLabel.alpha = 0.0
        }, completion: {(isCompleted) in
            toastLabel.removeFromSuperview()
        })
    }
//    @objc func usingBrigde(){
//        print("run function")
//        self.brigde_?.eventDispatcher()?.sendAppEvent(withName: "EMIT_GET_TEST", body: "test brigde")
//    }
    
}
