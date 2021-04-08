package com.arcorereactnative;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

public class ArCoreModuleViewManager extends SimpleViewManager<ArCoreView> {
  private ArCoreView arCoreView;

  public ArCoreModuleViewManager(ReactApplicationContext reactContext) {

  }


  @NonNull
  @Override
  public String getName() {
    return "arCoreModuleViewManager";
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @NonNull
  @Override
  protected ArCoreView createViewInstance(@NonNull ThemedReactContext reactContext) {
    arCoreView = new ArCoreView(reactContext);
    return arCoreView;
  }

  public ArCoreView getArCoreView() {
    return arCoreView;
  }
}
