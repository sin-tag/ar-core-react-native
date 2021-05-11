//
//  Object.swift
//  ar-core-react-native
//
//  Created by D2T_IOS2 on 28/04/2021.
//

import Foundation

@available(iOS 11.3, *)
class ObjectAVR: VirtualObject {

    override init() {
        super.init(modelName:"ship.scn")
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
