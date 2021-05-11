// create by hoangtuyensk@gmail.com - github: sig-tag
package com.arcorereactnative;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.File;

public class ArCoreView extends FrameLayout {
  public ReactActivity reactActivity = null;
  private ThemedReactContext context;
  private ArFragment arFragment;
  private Renderable objectRender;
  private boolean multiObject = false;
  private AnchorNode anchorNodeDelete;
  private String idItem = "";
  private boolean returnID = false;

  @RequiresApi(api = Build.VERSION_CODES.N)
  public ArCoreView(ThemedReactContext context) {
    super(context);
    this.context = context;
    this.reactActivity = (ReactActivity) context.getCurrentActivity();
    init();

  }

  @SuppressLint("ShowToast")
  @RequiresApi(api = Build.VERSION_CODES.N)
  public void init() {
    if (!checkIsSupportedDeviceOrFinish((AppCompatActivity) context.getCurrentActivity())) {
      return;
    }
    inflate((AppCompatActivity) context.getCurrentActivity(), R.layout.activity_main, this);

    arFragment = (ArFragment) ((AppCompatActivity) context.getCurrentActivity()).getSupportFragmentManager().findFragmentById(R.id.ui_fragment);
    assert arFragment != null;
    arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
      if (objectRender == null) {
        Toast.makeText(reactActivity, "Not found object loader", Toast.LENGTH_LONG);
        Log.e("LOAD_OBJECT", "Can't not find from cache");
        return;
      }
      if (!multiObject) {
        Log.e("LOAD_OBJECT", "Block event add object");
        return;
      }
      Anchor anchor = hitResult.createAnchor();
      AnchorNode anchorNode = new AnchorNode(anchor);
      anchorNode.setParent(arFragment.getArSceneView().getScene());
      anchorNode.setName(idItem);

      // add
      TransformableNode object = new TransformableNode(arFragment.getTransformationSystem());
      object.setParent(anchorNode);
      object.setRenderable(objectRender);
      object.select();
      object.setLocalScale(new Vector3(1.0f, 1.0f, 1.0f));
      object.getRotationController().setEnabled(true);
      object.getScaleController().setEnabled(true);
      object.setOnTouchListener(new Node.OnTouchListener() {
        @Override
        public boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {

          if (anchorNodeDelete != null && anchorNodeDelete.getName().equalsIgnoreCase(anchorNode.getName())) {
            if (returnID) {
              anchorNodeDelete = anchorNode;
              WritableMap map = Arguments.createMap();
              map.putString("ID", anchorNodeDelete.getName());
              map.putString("SELECTED", "TRUE");
              ModuleWithEmitter.sendEvent(context, ModuleWithEmitter.EMIT_GET_NAME, map);

              returnID = false;
            }
          } else {
            anchorNodeDelete = anchorNode;
            WritableMap map = Arguments.createMap();
            map.putString("ID", "");
            map.putString("SELECTED", "TRUE");
            ModuleWithEmitter.sendEvent(context, ModuleWithEmitter.EMIT_GET_NAME, map);
            returnID = false;
          }
          return true;
        }
      });
      multiObject = true;
    });
    arFragment.getArSceneView().getScene().setOnTouchListener(new Scene.OnTouchListener() {
      @Override
      public boolean onSceneTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
        if (anchorNodeDelete == null) {
          returnID = true;
        } else {
          WritableMap map = Arguments.createMap();
          map.putString("ID", "");
          map.putString("SELECTED", "FALSE");
          ModuleWithEmitter.sendEvent(context, ModuleWithEmitter.EMIT_GET_NAME, map);
          anchorNodeDelete = null;
        }
        return true;
      }
    });
  }

  public void getNameWithEmitter() {
    WritableMap map = Arguments.createMap();
    map.putString("IdProduct", "");
    ModuleWithEmitter.sendEvent(context, ModuleWithEmitter.EMIT_GET_NAME, map);
  }


  @RequiresApi(api = Build.VERSION_CODES.N)
  public boolean setObject(String uriString) {
    AtomicBoolean loadComplete = new AtomicBoolean(true);
    WeakReference<ArCoreView> weakActivity = new WeakReference<>(this);
    Uri uri = Uri.fromFile(new File(uriString));
    Log.e("PATH_FILE", uri.toString());
    ModelRenderable.builder()
      .setSource((AppCompatActivity) context.getCurrentActivity(), uri)
      .setIsFilamentGltf(true)
      .build()
      .thenAccept(
        modelRenderable -> {
          ArCoreView activity = weakActivity.get();
          if (activity != null) {
            activity.objectRender = modelRenderable;
          }
        })
      .exceptionally(
        throwable -> {
          Toast toast =
            Toast.makeText((AppCompatActivity) context.getCurrentActivity(), "Unable to load Tiger renderable", Toast.LENGTH_LONG);
          toast.setGravity(Gravity.CENTER, 0, 0);
          toast.show();
          Log.e("Load", "Load obj");
          return null;
        });
    Toast.makeText((AppCompatActivity) context.getCurrentActivity(), "Select object complete", Toast.LENGTH_LONG);
    Log.d("CMD_RUN_SET_OBJECT", "Load object complete");
    multiObject = true;
    return loadComplete.get();
  }

  @SuppressLint("SetTextI18n")
  public void calcDistanceToObject() {
    Frame frame = arFragment.getArSceneView().getArFrame();
    Pose camera = frame.getCamera().getPose();
    Vector3 objectMatrix = anchorNodeDelete.getWorldPosition();
    Double distance = calculateDistance(objectMatrix, camera);

  }

  private Double calculateDistance(Vector3 objectPose0, Pose objectPose1) {
    return calculateDistance(
      objectPose0.x - objectPose1.tx(),
      objectPose0.y - objectPose1.ty(),
      objectPose0.z - objectPose1.tz()
    );
  }

  private Double calculateDistance(Float x, Float y, Float z) {
    return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
  }

  @SuppressLint("ShowToast")
  public void deleteNodeObject() {
    if (anchorNodeDelete == null) {
      Toast.makeText((AppCompatActivity) context.getCurrentActivity(), "Can't choose object", Toast.LENGTH_LONG);
    } else {
      arFragment.getArSceneView().getScene().removeChild(anchorNodeDelete);
      anchorNodeDelete.getAnchor().detach();
      anchorNodeDelete.setParent(null);
      anchorNodeDelete = null;
      idItem = "";
      Log.d("CMD_DELETE_OBJECT", "Delete object complete!");
    }


  }

  public String getIdItem() {
    return idItem;
  }

  public void setIdItem(String item) {
    this.idItem = item;
  }

  public void setMultiObject() {
    multiObject = true;
  }

  public void killProcess() {
    try {
      Log.e("REMOVE_KILL", "RUN START");
      ((AppCompatActivity) context.getCurrentActivity()).getSupportFragmentManager().beginTransaction().remove(arFragment).commitAllowingStateLoss();
      Thread threadPause = new Thread() {
        @Override
        public void run() {
          try {
            sleep(100);
            Objects.requireNonNull(arFragment.getArSceneView().getSession()).pause();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };
      Thread thread = new Thread() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
          try {
            threadPause.start();
            sleep(100);
            Objects.requireNonNull(arFragment.getArSceneView().getSession()).close();
            ((AppCompatActivity) context.getCurrentActivity()).finish();
          } catch (Exception e) {
            e.printStackTrace();
          } catch (Throwable throwable) {
            throwable.printStackTrace();
          } finally {
            System.gc();
            Log.e("REMOVE_KILL", "END START");
          }
        }
      };
      thread.start();
    } catch (Exception e) {
      System.out.println(e.toString());
    }

  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    
    ((AppCompatActivity) context.getCurrentActivity()).getSupportFragmentManager().beginTransaction().remove(arFragment).commitAllowingStateLoss();
    Thread threadPause = new Thread() {
      @Override
      public void run() {
        try {
          sleep(100);
          Objects.requireNonNull(arFragment.getArSceneView().getSession()).pause();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          threadPause.start();
          sleep(500);
          Objects.requireNonNull(arFragment.getArSceneView().getSession()).close();
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          Log.e("END_", "Finish");
        }
      }
    };
    thread.start();

  }

  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      Log.e("ArCoreView", "ArCore requires Android N or later");
      Toast.makeText(activity, "ArCore requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
      ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
        .getDeviceConfigurationInfo()
        .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < 3.0) {
      Log.e("ArCoreView", "ArCore requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "ArCore requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
        .show();
      activity.finish();
      return false;
    }
    return true;
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void screenShort() {
    ArSceneView view = arFragment.getArSceneView();

    // Create a bitmap the size of the scene view.
    final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
      Bitmap.Config.ARGB_8888);

    // Create a handler thread to offload the processing of the image.
    final HandlerThread handlerThread = new HandlerThread("PixelCopier");
    handlerThread.start();
    // Make the request to copy.
    PixelCopy.request(view, bitmap, (copyResult) -> {
      if (copyResult == PixelCopy.SUCCESS) {
        try {
          saveBitmapToDisk(bitmap);
        } catch (IOException e) {
          Toast toast = Toast.makeText(((AppCompatActivity) context.getCurrentActivity()), e.toString(),
            Toast.LENGTH_LONG);
          toast.show();
          WritableMap map = Arguments.createMap();
          map.putString("PATH_FILE", "");
          ModuleWithEmitter.sendEvent(context, ModuleWithEmitter.EMIT_GET_PATH_FILE_SCREEN_SHORT, map);
          return;
        }
      } else {
      }
      handlerThread.quitSafely();
    }, new Handler(handlerThread.getLooper()));
  }


  public void saveBitmapToDisk(Bitmap bitmap) throws IOException {
    File imageScreen = new File(context.getDir("images", Context.MODE_PRIVATE).toString());
    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("HH.mm.ss dd-MM-yyyy");
    String formattedDate = df.format(c.getTime());

    File mediaFile = new File(imageScreen, "AVRScreenShort" + formattedDate + ".jpeg");

    FileOutputStream fileOutputStream = new FileOutputStream(mediaFile);
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream);
    fileOutputStream.flush();
    fileOutputStream.close();
    WritableMap map = Arguments.createMap();
    map.putString("PATH_FILE", mediaFile.getPath());
    ModuleWithEmitter.sendEvent(context, ModuleWithEmitter.EMIT_GET_PATH_FILE_SCREEN_SHORT, map);
  }
}
