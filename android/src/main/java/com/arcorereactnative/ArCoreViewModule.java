package com.arcorereactnative;

import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.ar.sceneform.rendering.ModelRenderable;

public class ArCoreViewModule extends ReactContextBaseJavaModule {
  private ArCoreModuleViewManager arCoreView;

  @RequiresApi(api = Build.VERSION_CODES.N)
  ArCoreViewModule(ReactApplicationContext context, ArCoreModuleViewManager viewManager) {
    super(context);
//    Log.e("MAM", viewManager.getArCoreView().toString());
    if (viewManager != null) {
      Log.d("viewManager", "Not Null");
      arCoreView = viewManager;
    } else {
      Log.d("viewManager", "Null viewManager");
    }

  }

  @Override
  public String getName() {
    return "ArCoreViewModule";
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @ReactMethod
  public void changeObject(String path) {
    Log.e("OBJECT", ArCoreModuleViewManager.arCoreView.toString());
    ArCoreModuleViewManager.arCoreView.changeObject(path);
  }

}
