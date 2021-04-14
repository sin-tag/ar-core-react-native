package com.arcorereactnative;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class ModuleWithEmitter {
  public static final String EMIT_GET_NAME = "EMIT_GET_NAME";
  public static final String EMIT_APP_RESUME = "EMIT_APP_RESUME";
  public static final String EMIT_APP_PAUSE = "EMIT_APP_PAUSE";

  public static void sendEvent(ReactContext context, String name, WritableMap body) {
    if (context != null) {
      context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, body);
    }
  }
}
