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
  public static final int COMMAND_GET_NAME_OBJECT_SELECTED = 1;
  public static final int COMMAND_DELETE_OBJECT_SELECTED = 2;

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
      "getNameObjectSelected",
      COMMAND_GET_NAME_OBJECT_SELECTED,
      "deleteObjectSelected",
      COMMAND_DELETE_OBJECT_SELECTED
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

  @ReactProp(name = "idObject")
  public void setNameObject(ArCoreView arCoreView, String idObject) {

  }

  @Override
  public void receiveCommand(@NonNull ArCoreView root, String commandId, @Nullable ReadableArray args) {
    super.receiveCommand(root, commandId, args);
    switch (commandId) {
      case "getNameObjectSelected":
        String idName = root.getIdItem();

        break;
      case "deleteObjectSelected":
        break;
      default:
        break;
    }

  }

  @ReactProp(name = "delete")
  public void deleteObject(ArCoreView arCoreView, boolean delete) {
    arCoreView.deleteNodeObject(delete);
  }

}
