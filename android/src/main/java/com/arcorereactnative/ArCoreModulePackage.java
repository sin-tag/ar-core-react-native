package com.arcorereactnative;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.List;

public class ArCoreModulePackage implements ReactPackage {

  @RequiresApi(api = Build.VERSION_CODES.N)
  @NonNull
  @Override
  public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {

    return Arrays.<ViewManager>asList(new ArCoreModuleViewManager(reactContext));
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @NonNull
  @Override
  public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
    return Arrays.<NativeModule>asList(new ArCoreViewModule(reactContext));
  }
}
