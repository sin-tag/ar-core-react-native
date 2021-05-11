//
//  virtualObjectsManager.swift
//  ARKitProject
//
//  create by hoangtuyensk@gmail.com - github: sig-tag
//  Copyright © 2017 Apple. All rights reserved.
//

import Foundation
import os.log

@available(iOS 11.0, *)
class VirtualObjectsManager {

	static let shared = VirtualObjectsManager()

	// AutoIncrement Unique Id
	private var nextID = 1
	func generateUid() -> Int {
		nextID += 1
		return nextID
	}

	private var virtualObjects: [VirtualObject] = [VirtualObject]()
	private var virtualObjectSelected: VirtualObject?

	func addVirtualObject(virtualObject: VirtualObject) {
		virtualObjects.append(virtualObject)
        self.virtualObjectSelected = virtualObject
	}

	func resetVirtualObjects() {
//        if (virtualObjects.count >= 1){
//            setVirtualObjectSelected(virtualObject :self.virtualObjects[0])
//        }
        self.virtualObjectSelected = nil
    
	}

	func removeVirtualObject(virtualObject: VirtualObject) {
		if let index = virtualObjects.index(where: { $0.id == virtualObject.id }) {
			virtualObjects.remove(at: index)
		} else {
			os_log("Element not found", type: .error)
		}
	}

	func removeVirtualObjectSelected() {
		guard let object = virtualObjectSelected else {
			return
		}

		removeVirtualObject(virtualObject: object)
		object.unloadModel()
		object.removeFromParentNode()
		virtualObjectSelected = nil
	}

	func getVirtualObjects() -> [VirtualObject] {
		return self.virtualObjects
	}

	func isAVirtualObjectPlaced() -> Bool {
		return virtualObjectSelected != nil
	}

	func setVirtualObjectSelected(virtualObject: VirtualObject) {
        
        self.virtualObjectSelected = virtualObject
//        self.bridge.eventDispatcher()?.sendAppEvent(withName: "EMIT_GET_TEST", body: "send nef")
        
	}

	func getVirtualObjectSelected() -> VirtualObject? {
		return self.virtualObjectSelected
	}
    func setSelectedObjectByID(id:Int) {
        for object in self.virtualObjects{
            if object.id == id {
                self.virtualObjectSelected = object
                return
            }
        }
    }
    func getAllObjects() -> [VirtualObject]{
        return self.virtualObjects
    }
}
