package com.reactnativeawesomemodule;

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
  private static ModelRenderable object;
  private ArCoreView arCoreView;

  @RequiresApi(api = Build.VERSION_CODES.N)
  ArCoreViewModule(ReactApplicationContext context, ArCoreModuleViewManager viewManager) {
    super(context);
    if (viewManager != null) {
      arCoreView = viewManager.getArCoreView();
    }
  }

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
  public void setFileObject(String path) {
    setObjectFromPath(path);
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public static void setObjectFromPath(String file) {
    Uri uri = Uri.parse(file);
    ModelRenderable.builder()
      .setSource(ArCoreView.getActivity(), uri)
      .build()
      .thenAccept(modelRenderable -> object = modelRenderable)
      .exceptionally(
        throwable -> {
          Log.e("ERR", "Unable to load andy renderable", throwable);
          Toast toast =
            Toast.makeText(ArCoreView.getActivity(), "Loi load model:" + throwable.toString(), Toast.LENGTH_LONG);
          toast.setGravity(Gravity.CENTER, 0, 0);
          toast.show();
          return null;
        }
      );
  }
}
