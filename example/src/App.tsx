import * as React from 'react';

import { StyleSheet, View, TouchableOpacity, UIManager, findNodeHandle, Text } from 'react-native';
import ArCoreReactNativeViewManager from 'ar-core-react-native';

export default class App extends React.Component {
  render() {
    return (
      <View style={styles.container}>

        <ArCoreReactNativeViewManager color="yellow" style={styles.box} ref="arCoreView" />
        <TouchableOpacity onPress={() => {
          UIManager.dispatchViewManagerCommand(
            findNodeHandle(this.refs.arCoreView),
            "CMD_RUN_SET_OBJECT",
            ["001", ""]);
        }} style={styles.buttonAction}>
          <Text>Add Object</Text>
        </TouchableOpacity>
      </View>
    )
  }

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
  buttonAction: {
    position: 'absolute', 
    top: 30, left: 0, right: 0, 
    justifyContent: 'center', alignItems: 'center'
  }
});
