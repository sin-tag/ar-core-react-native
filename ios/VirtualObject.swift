import Foundation
import SceneKit
import ARKit

@available(iOS 11.0, *)
class VirtualObject: SCNNode {
	static let ROOT_NAME = "Virtual object root node"
	var modelName: String = ""
	var modelLoaded: Bool = false
	var id: Int!
    var idProduct: String = ""
    var wrapperNode = SCNNode();
    var node_ = SCNNode();

	var viewController: ArCoreReactNativeView?

	override init() {
		super.init()
		self.name = VirtualObject.ROOT_NAME
	}

    init(modelName: String, idProduct: String) {
		super.init()
		self.id = VirtualObjectsManager.shared.generateUid()
		self.name = VirtualObject.ROOT_NAME
		self.modelName = modelName
        self.idProduct = idProduct
	}

	required init?(coder aDecoder: NSCoder) {
		fatalError("init(coder:) has not been implemented")
	}
    func loadModel() -> Bool {
        print("model name:", self.modelName)
//        guard let url = Bundle.main.url(forResource: modelName, withExtension: "usdz") else { fatalError() }
		guard let virtualObjectScene = try? SCNScene(url: URL(fileURLWithPath: modelName), options: [.checkConsistency: true]) else{
			print("")
            return false
		}
//        guard let virtualObjectScene = try? SCNScene(named: "cherub.dae") else{
//            print("")
//            return false
//        }
		for child in virtualObjectScene.rootNode.childNodes {
			child.geometry?.firstMaterial?.lightingModel = .physicallyBased
			child.movabilityHint = .movable
            child.name = self.idProduct + String(self.id)
            self.node_ = child
			wrapperNode.addChildNode(child)
                
		}
//        wrapperNode.position = vector
//        print("Have Object with name:", modelName)
        self.addChildNode(wrapperNode)
        print(wrapperNode.position)
		modelLoaded = true
        return true
	}

    func getName()-> String{
        return self.idProduct + String(self.id)
    }
    func getId()->Int{
        return self.id
    }
    func getObject() -> SCNVector3{
        return wrapperNode.position
    }
    func getNode() -> SCNNode{
        return node_
    }
	func unloadModel() {
		for child in self.childNodes {
			child.removeFromParentNode()
		}

		modelLoaded = false
	}


	func translateBasedOnScreenPos(_ pos: CGPoint, instantly: Bool, infinitePlane: Bool) {
		guard let controller = viewController else {
			return
		}

		let result = controller.worldPositionFromScreenPosition(pos, objectPos: self.position, infinitePlane: infinitePlane)
		controller.moveVirtualObjectToPosition(result.position, instantly, !result.hitAPlane)
	}
}

@available(iOS 11.0, *)
extension VirtualObject {

	static func isNodePartOfVirtualObject(_ node: SCNNode) -> Bool {
		if node.name == VirtualObject.ROOT_NAME {
			return true
		}

		if node.parent != nil {
			return isNodePartOfVirtualObject(node.parent!)
		}

		return false
	}
}

// MARK: - Protocols for Virtual Objects

protocol ReactsToScale {
	func reactToScale()
}

extension SCNNode {

	func reactsToScale() -> ReactsToScale? {
		if let canReact = self as? ReactsToScale {
			return canReact
		}

		if parent != nil {
			return parent!.reactsToScale()
		}

		return nil
	}
}
