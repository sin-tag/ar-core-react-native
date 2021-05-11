import { requireNativeComponent, ViewStyle } from 'react-native';

type ArCoreReactNativeProps = {
  color: string;
  style: ViewStyle;
};

export const ArCoreReactNativeViewManager = requireNativeComponent<ArCoreReactNativeProps>(
  'ArCoreReactNativeView'
);

export default ArCoreReactNativeViewManager;
