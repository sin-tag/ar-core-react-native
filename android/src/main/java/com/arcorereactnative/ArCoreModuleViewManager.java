package com.arcorereactnative;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;

public class ArCoreModuleViewManager extends ViewGroupManager<ArCoreView> {
  private ArCoreView arCoreView;

  public ArCoreModuleViewManager(ReactApplicationContext reactContext) {
    super();
  }


  @NonNull
  @Override
  public String getName() {
    return "arCoreModuleViewManager";
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  public ArCoreView createViewInstance(@NonNull ThemedReactContext reactContext) {
    arCoreView = new ArCoreView(reactContext);
    return arCoreView;
  }

  public ArCoreView getArCoreView() {
    return arCoreView;
  }
}
