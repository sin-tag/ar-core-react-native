package com.arcorereactnative;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class ArCoreViewModule extends ReactContextBaseJavaModule {

  @RequiresApi(api = Build.VERSION_CODES.N)
  ArCoreViewModule(ReactApplicationContext context) {
    super(context);

  }

  @Override
  public String getName() {
    return "ArCoreViewModule";
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @ReactMethod
  public void changeObject(String path, ArCoreView arCoreView) {
    Log.e("OBJECT", arCoreView.toString());
    arCoreView.changeObject(path);
  }

}
