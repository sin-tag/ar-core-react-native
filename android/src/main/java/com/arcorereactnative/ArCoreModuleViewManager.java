package com.arcorereactnative;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
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
import java.util.concurrent.ExecutionException;


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
  @ReactProp(name = "objectName")
  public void setObject(ArCoreView arCoreView, String objectName) {
//    String value = new DownloadFileFromURL().execute("http://shop.avr.asia/admin/media/chair.sfb").get();
    arCoreView.setObject(object_name);
  }

  @ReactProp(name = "idObject")
  public void setNameObject(ArCoreView arCoreView, String idObject)

  @ReactProp(name = "delete")
  public void deleteObject(ArCoreView arCoreView, boolean delete) {
    arCoreView.deleteNodeObject(delete);
  }

  public void createFolderSaveObject() {
    File folder = new File(Environment.getExternalStorageDirectory() +
      File.separator + "TollCulator");
    boolean success = true;
    if (!folder.exists()) {
      success = folder.mkdirs();
    }
    if (success) {
      // Do something on success
    } else {
      // Do something else on failure
    }
  }

  class DownloadFileFromURL extends AsyncTask<String, String, String> {

    /**
     * Before starting background thread Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(String... f_url) {
      int count;
      try {
        URL url = new URL(f_url[0]);
        URLConnection connection = url.openConnection();
        connection.connect();

        // this will be useful so that you can show a tipical 0-100%
        // progress bar
        int lenghtOfFile = connection.getContentLength();

        // download the file
        InputStream input = new BufferedInputStream(url.openStream(),
          8192);

        // Output stream
        OutputStream output = new FileOutputStream(arCoreView.getContext().getCacheDir().toString() +
          "/" + f_url[0].split("/")[f_url[0].split("/").length - 1]);
        System.out.println("FILE:" + arCoreView.getContext().getDir("sampledata", Context.MODE_PRIVATE).toString());
        byte data[] = new byte[1024];

        long total = 0;

        while ((count = input.read(data)) != -1) {
          total += count;
          // publishing the progress....
          // After this onProgressUpdate will be called
          publishProgress("" + (int) ((total * 100) / lenghtOfFile));

          // writing data to file
          output.write(data, 0, count);
        }

        // flushing output
        output.flush();

        // closing streams
        output.close();
        input.close();
        return "chair.sfb";

      } catch (Exception e) {
        Log.e("Error: ", e.getMessage());
      }

      return null;
    }


  }
}
