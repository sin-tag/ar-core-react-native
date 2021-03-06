// create by hoangtuyensk@gmail.com - github: sig-tag
package com.arcorereactnative;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class ModuleWithEmitter {
  public static final String EMIT_GET_NAME = "EMIT_GET_NAME";
  public static final String EMIT_GET_PATH_FILE_SCREEN_SHORT = "EMIT_GET_PATH_FILE_SCREEN_SHORT";

  public static void sendEvent(ReactContext context, String name, WritableMap body) {
    if (context != null) {
      context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, body);
    }
  }
}
