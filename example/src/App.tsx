import * as React from 'react';

import { StyleSheet, View, Text, TouchableOpacity, NativeModules } from 'react-native';
import AwesomeModuleViewManager from 'ar-core-react-native';

export default function App() {
  return (
    <View style={styles.container}>
      <AwesomeModuleViewManager color="#32a852" style={styles.box} />
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
    marginVertical: 20,
  },
});
