import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import ArCoreReactNativeViewManager from 'ar-core-react-native';

export default function App() {
  return (
    <View style={styles.container}>
      <ArCoreReactNativeViewManager color="yellow" style={styles.box} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: '100%',
    height: '100%',
    // marginVertical: 20,
  },
});
