# Our library is under development again promising to bring high performance, recently our team had to go to advanced training in 3D, so no one developed and fixed bugs. Sorry guys very much.
# ar-core-react-native
- ar-core-react-native build from ARKit on IOS and ARCore on Android.
- It is opensource but performance of ios and android not optimal.
- If you want to develop AR/VR on Mobile for commercial purposes please contact me via email on github.
# View
## Demo in IOS
<img src="https://github.com/sin-tag/ar-core-react-native/blob/main/demo/Example2.png?raw=true" width="750" height="100%"/>

## Demo in Android
<img src="https://github.com/sin-tag/ar-core-react-native/blob/main/demo/android1.jpg?raw=true" width="425" height="100%"/>
<img src="https://github.com/sin-tag/ar-core-react-native/blob/main/demo/android2.jpg?raw=true" width="425" height="100%"/>

# Suport Object Type
## In Android
- type object 3d support is glb

## In IOS 
- type object 3d support is scn, usdz, obj
# Installation
```sh
npm install ar-core-react-native
```
# Usage
```js
import ArCoreReactNativeViewManager from "ar-core-react-native";
```
### Add Object
```js
import { UIManager, findNodeHandle} from 'react-native';
import ArCoreReactNativeViewManager from "ar-core-react-native";
...
<ArCoreReactNativeViewManager ref="arCoreView" />
...
// on action button or any
// you send 2 parameter
// name_object - type:string : name object in 3D view.
// path_file - type:string : path file to glb in device.
function addObject(){
    UIManager.dispatchViewManagerCommand(
        findNodeHandle(this.refs.arCoreView),
        "CMD_RUN_SET_OBJECT",
        [name_object, path_file]);
}
```
```
if you want morre object you can call function addObject() with new parameter.
```
### Delete Object Seleted
```js
// on action delete
function deleteObjectSeleted(){
    UIManager.dispatchViewManagerCommand(
        findNodeHandle(this.refs.arCoreView),
        "CMD_RUN_DELETE_OBJECT",
        []);
}
```

# Example
Read example in 
<a href="https://github.com/sin-tag/ar-core-react-native/tree/main/example">Example</a>
# License
MIT
