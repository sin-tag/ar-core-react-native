package com.arcorereactnative;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;

public class ArCoreModuleViewManager extends ViewGroupManager<ArCoreView> {
  @SuppressLint("StaticFieldLeak")
  public ArCoreView arCoreView;

  @RequiresApi(api = Build.VERSION_CODES.N)
  public ArCoreModuleViewManager(ReactApplicationContext reactContext) {
    super();
  }


  @NonNull
  @Override
  public String getName() {
    return "arCoreModuleViewManager";
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @NonNull
  @Override
  public ArCoreView createViewInstance(ThemedReactContext reactContext) {
    arCoreView = new ArCoreView(reactContext);
    if (arCoreView == null) {
      Log.e("NULL_arCoreViews", "arCoreViews.toString()");
    }
    else {
      Log.e("NULL_arCoreViews", "Not NULL");
    }
    return arCoreView;
  }


  public ArCoreView getArCoreViews() {
    if (arCoreView == null) {
      Log.e("NULL_IF", "arCoreViews.toString()");
    } else {
      Log.e("NULL_ELSE", arCoreView.toString());
    }

    return arCoreView;
  }
}
