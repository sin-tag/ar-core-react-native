package com.arcorereactnative;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class ArCoreModuleViewManager extends ViewGroupManager<ArCoreView> {
  @SuppressLint("StaticFieldLeak")
  public ArCoreView arCoreView;
  public static final int CMD_RUN_DELETE_OBJECT = 1;
  public static final int CMD_RUN_SET_OBJECT = 2;

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

  @Nullable
  @Override
  public Map<String, Integer> getCommandsMap() {
    return MapBuilder.of(
      "CMD_RUN_DELETE_OBJECT",
      CMD_RUN_DELETE_OBJECT,
      "CMD_RUN_SET_OBJECT",
      CMD_RUN_SET_OBJECT
    );
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
  @ReactProp(name = "objectName")
  public void setObject(ArCoreView arCoreView, String objectName) {
    arCoreView.setObject(objectName);
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  public void receiveCommand(@NonNull ArCoreView root, String commandId, @Nullable ReadableArray args) {
    super.receiveCommand(root, commandId, args);
    switch (commandId) {
      case "CMD_RUN_DELETE_OBJECT":
        root.deleteNodeObject();
        break;
      case "CMD_RUN_CLEAN_OBJECT":
        assert args != null;
        Log.e("ARGS", args.getString(0));
        Log.e("ARGS", args.getString(1));
        break;
      case "CMD_RUN_SET_OBJECT":
        assert args != null;
        String idProduct = args.getString(0);
        String pathFile = args.getString(1);
        root.setIdItem(idProduct);
        root.setObject(pathFile);
        break;
      default:
        break;
    }

  }

}
