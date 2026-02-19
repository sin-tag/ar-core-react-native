import React, { forwardRef, useRef, useImperativeHandle, useCallback } from 'react';
import {
  requireNativeComponent,
  UIManager,
  findNodeHandle,
} from 'react-native';

import type { ArCoreReactNativeProps, ArCoreReactNativeRef } from './types';

export type { ArCoreReactNativeProps, ArCoreReactNativeRef } from './types';
export type {
  PlaneDetection,
  ObjectSelectedEvent,
  ScreenshotEvent,
  PlaneDetectedEvent,
  TrackingStateEvent,
} from './types';

const NATIVE_VIEW_NAME = 'ArCoreReactNativeView';

const NativeArView =
  requireNativeComponent<ArCoreReactNativeProps>(NATIVE_VIEW_NAME);

function getCommandId(command: string): number | string {
  const commands = (UIManager as any).getViewManagerConfig?.(NATIVE_VIEW_NAME)
    ?.Commands;
  if (commands && commands[command] !== undefined) {
    return commands[command];
  }
  return command;
}

const ArCoreReactNativeView = forwardRef<
  ArCoreReactNativeRef,
  ArCoreReactNativeProps
>((props, ref) => {
  const nativeRef = useRef(null);

  const dispatchCommand = useCallback(
    (command: string, args: any[] = []) => {
      const handle = findNodeHandle(nativeRef.current);
      if (handle != null) {
        const commandId = getCommandId(command);
        UIManager.dispatchViewManagerCommand(handle, commandId, args);
      }
    },
    []
  );

  useImperativeHandle(
    ref,
    () => ({
      addObject(modelPath: string, id?: string) {
        dispatchCommand('addObject', [modelPath, id || '']);
      },
      removeSelectedObject() {
        dispatchCommand('removeSelectedObject');
      },
      takeScreenshot() {
        dispatchCommand('takeScreenshot');
      },
      setDuplicate() {
        dispatchCommand('setDuplicate');
      },
      reset() {
        dispatchCommand('reset');
      },
    }),
    [dispatchCommand]
  );

  const {
    style,
    planeDetection = 'both',
    lightEstimation = true,
    showPlanes = false,
    showFeaturePoints = false,
    onObjectSelected,
    onScreenshot,
    onPlaneDetected,
    onTrackingStateChange,
    ...restProps
  } = props;

  return (
    <NativeArView
      ref={nativeRef}
      style={style}
      planeDetection={planeDetection}
      lightEstimation={lightEstimation}
      showPlanes={showPlanes}
      showFeaturePoints={showFeaturePoints}
      onObjectSelected={onObjectSelected}
      onScreenshot={onScreenshot}
      onPlaneDetected={onPlaneDetected}
      onTrackingStateChange={onTrackingStateChange}
      {...restProps}
    />
  );
});

ArCoreReactNativeView.displayName = 'ArCoreReactNativeView';

export default ArCoreReactNativeView;
