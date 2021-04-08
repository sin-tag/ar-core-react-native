package com.arcorereactnative;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArCoreModulePackage implements ReactPackage {
  private ArCoreModuleViewManager arCoreModuleViewManager;

  @RequiresApi(api = Build.VERSION_CODES.N)
  @NonNull
  @Override
  public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
    if (this.arCoreModuleViewManager == null || this.arCoreModuleViewManager.getArCoreViews() == null) {
      Log.e("BUILD", "createViewManagers");
      this.arCoreModuleViewManager = new ArCoreModuleViewManager(reactContext);
    }
    return Arrays.<ViewManager>asList(this.arCoreModuleViewManager);
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @NonNull
  @Override
  public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
    if (this.arCoreModuleViewManager == null || this.arCoreModuleViewManager.getArCoreViews() == null) {
      this.arCoreModuleViewManager = new ArCoreModuleViewManager(reactContext);
      Log.e("BUILD", "createNativeModules");
    }
    return Arrays.<NativeModule>asList(new ArCoreViewModule(reactContext, this.arCoreModuleViewManager));
  }
}
