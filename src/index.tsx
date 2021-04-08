import { requireNativeComponent, ViewStyle } from 'react-native';

type AwesomeModuleProps = {
  color: string;
  style: ViewStyle;
};

export const AwesomeModuleViewManager = requireNativeComponent<AwesomeModuleProps>(
  'AwesomeModuleView'
);

export default AwesomeModuleViewManager;
