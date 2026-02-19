import type { ViewStyle, StyleProp, NativeSyntheticEvent } from 'react-native';

export type PlaneDetection = 'horizontal' | 'vertical' | 'both' | 'none';

export interface ObjectSelectedEvent {
  id: string;
  selected: boolean;
}

export interface ScreenshotEvent {
  path: string;
}

export interface PlaneDetectedEvent {
  id: string;
  type: 'horizontal' | 'vertical';
}

export interface TrackingStateEvent {
  state: 'normal' | 'limited' | 'notAvailable';
  reason?: string;
}

export interface ArCoreReactNativeProps {
  style?: StyleProp<ViewStyle>;

  /** Type of plane detection. Default: 'both' */
  planeDetection?: PlaneDetection;

  /** Enable light estimation. Default: true */
  lightEstimation?: boolean;

  /** Show detected planes overlay. Default: false */
  showPlanes?: boolean;

  /** Show feature points. Default: false */
  showFeaturePoints?: boolean;

  /** Called when an object is selected/deselected */
  onObjectSelected?: (event: NativeSyntheticEvent<ObjectSelectedEvent>) => void;

  /** Called when a screenshot is taken */
  onScreenshot?: (event: NativeSyntheticEvent<ScreenshotEvent>) => void;

  /** Called when a new plane is detected */
  onPlaneDetected?: (event: NativeSyntheticEvent<PlaneDetectedEvent>) => void;

  /** Called when AR tracking state changes */
  onTrackingStateChange?: (event: NativeSyntheticEvent<TrackingStateEvent>) => void;
}

export interface ArCoreReactNativeRef {
  /** Add a 3D object to the AR scene */
  addObject(modelPath: string, id?: string): void;

  /** Remove the currently selected object */
  removeSelectedObject(): void;

  /** Take a screenshot of the AR scene */
  takeScreenshot(): void;

  /** Allow placing duplicate objects */
  setDuplicate(): void;

  /** Reset/destroy the AR session */
  reset(): void;
}
