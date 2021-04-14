// @ts-ignore
import * as React from 'react';
import {StyleSheet, View, Text, TouchableOpacity, NativeModules, UIManager, findNodeHandle} from 'react-native';
import AwesomeModuleViewManager from 'ar-core-react-native';
import RNModuleWithEmitter from './RNModuleWithEmitter';
export default function App() {
  return (
    <View style={styles.container}>
      <AwesomeModuleViewManager color="#32a852" style={styles.box} object_name="chair.sfb"/>
      <TouchableOpacity onPress={() => {
        RNModuleWithEmitter.addListener("EMIT_GET_NAME", (data) => {
          console.log(data);
        });
      }} style={{position: 'absolute', top: 30, left: 0, right: 0, justifyContent: 'center', alignItems: 'center'}}>
        <Text>Bam</Text>
      </TouchableOpacity>
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
