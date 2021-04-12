package com.arcorereactnative;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.ArrayList;

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
    } else {
      Log.e("NULL_arCoreViews", "Not NULL");
    }
    return arCoreView;
  }

  public ArCoreView getArCoreViews(ArCoreView arCoreView) {
    if (arCoreView == null) {
      Log.e("NULL_IF", "arCoreViews.toString()");
    } else {
      Log.e("NULL_ELSE", arCoreView.toString());
    }
    return arCoreView;
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @ReactProp(name = "object")
  public void setObject(ArCoreView arCoreView, String object, Promise promise) {
    boolean res = arCoreView.changeObject(object);
    promise.resolve(res);
    promise.reject("LOAD_MODEL", "Can't load model");
  }

  @ReactProp(name = "delete")
  public void deleteObject(ArCoreView arCoreView, boolean delete) {
    arCoreView.deleteNodeObject(delete);
  }
}
