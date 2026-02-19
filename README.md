# ar-core-react-native

ARCore (Android) and ARKit (iOS) for React Native.

A React Native component for building Augmented Reality experiences with proper props, ref methods, and event callbacks.

## Demo

### iOS
<img src="https://github.com/sin-tag/ar-core-react-native/blob/main/demo/Example2.png?raw=true" width="750" height="100%"/>

### Android
<img src="https://github.com/sin-tag/ar-core-react-native/blob/main/demo/android1.jpg?raw=true" width="425" height="100%"/>
<img src="https://github.com/sin-tag/ar-core-react-native/blob/main/demo/android2.jpg?raw=true" width="425" height="100%"/>

## Requirements

| Platform | Min Version | AR SDK |
|----------|-------------|--------|
| Android  | API 24 (Android 7.0) | ARCore 1.47.0 |
| iOS      | 13.0 | ARKit 3 |

## Supported 3D Object Types

| Platform | Formats |
|----------|---------|
| Android  | `.glb` (glTF Binary) |
| iOS      | `.scn`, `.usdz`, `.obj` |

## Installation

```sh
npm install ar-core-react-native
```

### Android

Make sure your `minSdkVersion` is at least **24** in `android/build.gradle`:

```gradle
minSdkVersion = 24
```

### iOS

Make sure your deployment target is at least **13.0**. Run:

```sh
cd ios && pod install
```

## Usage

### Basic Setup

```tsx
import React, { useRef } from 'react';
import { View } from 'react-native';
import ArCoreReactNativeView from 'ar-core-react-native';
import type { ArCoreReactNativeRef } from 'ar-core-react-native';

export default function App() {
  const arViewRef = useRef<ArCoreReactNativeRef>(null);

  return (
    <View style={{ flex: 1 }}>
      <ArCoreReactNativeView
        ref={arViewRef}
        style={{ width: '100%', height: '100%' }}
        planeDetection="both"
        lightEstimation={true}
        onObjectSelected={(event) => {
          const { id, selected } = event.nativeEvent;
          console.log(`Object ${id} selected: ${selected}`);
        }}
        onScreenshot={(event) => {
          console.log('Screenshot saved:', event.nativeEvent.path);
        }}
        onPlaneDetected={(event) => {
          console.log('Plane detected:', event.nativeEvent.type);
        }}
        onTrackingStateChange={(event) => {
          console.log('Tracking:', event.nativeEvent.state);
        }}
      />
    </View>
  );
}
```

### Add Object

```tsx
// Add a 3D model to the AR scene
arViewRef.current?.addObject('/path/to/model.glb', 'my-object-id');
```

### Remove Selected Object

```tsx
arViewRef.current?.removeSelectedObject();
```

### Take Screenshot

```tsx
arViewRef.current?.takeScreenshot();
// Result returned via onScreenshot callback
```

### Reset AR Session

```tsx
arViewRef.current?.reset();
```

## Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `planeDetection` | `'horizontal' \| 'vertical' \| 'both' \| 'none'` | `'both'` | Type of plane detection |
| `lightEstimation` | `boolean` | `true` | Enable light estimation |
| `showPlanes` | `boolean` | `false` | Show detected planes overlay |
| `showFeaturePoints` | `boolean` | `false` | Show AR feature points |

## Events

| Event | Payload | Description |
|-------|---------|-------------|
| `onObjectSelected` | `{ id: string, selected: boolean }` | Fired when an object is selected/deselected |
| `onScreenshot` | `{ path: string }` | Fired when a screenshot is taken |
| `onPlaneDetected` | `{ id: string, type: 'horizontal' \| 'vertical' }` | Fired when a new plane is detected |
| `onTrackingStateChange` | `{ state: 'normal' \| 'limited' \| 'notAvailable', reason?: string }` | Fired when AR tracking state changes |

## Ref Methods

| Method | Parameters | Description |
|--------|------------|-------------|
| `addObject` | `(modelPath: string, id?: string)` | Add a 3D model to the scene |
| `removeSelectedObject` | `()` | Remove the currently selected object |
| `takeScreenshot` | `()` | Capture the AR scene |
| `setDuplicate` | `()` | Allow placing duplicate objects |
| `reset` | `()` | Reset the AR session |

## Example

See the full example app:
[Example](https://github.com/sin-tag/ar-core-react-native/tree/main/example)

## License

MIT
