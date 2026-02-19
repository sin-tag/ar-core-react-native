import React, { useRef, useCallback } from 'react';
import { StyleSheet, View, TouchableOpacity, Text, Alert } from 'react-native';
import ArCoreReactNativeView from 'ar-core-react-native';
import type { ArCoreReactNativeRef } from 'ar-core-react-native';

export default function App() {
  const arViewRef = useRef<ArCoreReactNativeRef>(null);

  const handleAddObject = useCallback(() => {
    // Pass the path to a 3D model file and an optional ID
    arViewRef.current?.addObject('/path/to/model.glb', 'object-001');
  }, []);

  const handleRemoveObject = useCallback(() => {
    arViewRef.current?.removeSelectedObject();
  }, []);

  const handleScreenshot = useCallback(() => {
    arViewRef.current?.takeScreenshot();
  }, []);

  const handleReset = useCallback(() => {
    arViewRef.current?.reset();
  }, []);

  return (
    <View style={styles.container}>
      <ArCoreReactNativeView
        ref={arViewRef}
        style={styles.arView}
        planeDetection="both"
        lightEstimation={true}
        showFeaturePoints={false}
        onObjectSelected={(event) => {
          const { id, selected } = event.nativeEvent;
          console.log(`Object ${id} selected: ${selected}`);
        }}
        onScreenshot={(event) => {
          const { path } = event.nativeEvent;
          Alert.alert('Screenshot saved', path);
        }}
        onPlaneDetected={(event) => {
          const { id, type } = event.nativeEvent;
          console.log(`Plane detected: ${type} (${id})`);
        }}
        onTrackingStateChange={(event) => {
          const { state, reason } = event.nativeEvent;
          console.log(`Tracking: ${state} ${reason || ''}`);
        }}
      />

      <View style={styles.toolbar}>
        <TouchableOpacity onPress={handleAddObject} style={styles.button}>
          <Text style={styles.buttonText}>Add Object</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={handleRemoveObject} style={styles.button}>
          <Text style={styles.buttonText}>Remove</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={handleScreenshot} style={styles.button}>
          <Text style={styles.buttonText}>Screenshot</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={handleReset} style={[styles.button, styles.resetButton]}>
          <Text style={styles.buttonText}>Reset</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  arView: {
    width: '100%',
    height: '100%',
  },
  toolbar: {
    position: 'absolute',
    bottom: 40,
    left: 0,
    right: 0,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 10,
    paddingHorizontal: 16,
  },
  button: {
    backgroundColor: 'rgba(0,0,0,0.6)',
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 8,
  },
  resetButton: {
    backgroundColor: 'rgba(200,50,50,0.7)',
  },
  buttonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '600',
  },
});
