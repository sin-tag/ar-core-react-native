package com.arcorereactnative;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.atomic.AtomicBoolean;
import java.io.File;

public class ArCoreView extends FrameLayout {
  public static ReactActivity reactActivity = null;
  private ThemedReactContext context;

  private ArFragment arFragment;
  private ModelRenderable objectRender;
  private Float SCALE = 0.1f;
  private AnchorNode anchorNodeDelete;
  private AnchorNode anchorNodeSelected;
  private String idItem;
  private LinearLayout gestureLayout;

  public static void setContext(ReactActivity context) {
    reactActivity = context;
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public ArCoreView(ThemedReactContext context) {
    super(context);
    this.context = context;
    init();
  }

  @SuppressLint("ShowToast")
  @RequiresApi(api = Build.VERSION_CODES.N)
  public void init() {
    inflate(reactActivity, R.layout.activity_main, this);
    arFragment = (ArFragment) reactActivity.getSupportFragmentManager().findFragmentById(R.id.ui_fragment);
    arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
      if (objectRender == null) {
        Toast.makeText(reactActivity, "Not found object loader", Toast.LENGTH_LONG);
        Log.e("LOAD_OBJECT", "Can't not find from cache");
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
      object.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
      object.getRotationController().setEnabled(true);
      object.getScaleController().setEnabled(true);
      anchorNodeSelected = anchorNode;
      object.setOnTouchListener(new Node.OnTouchListener() {
        @Override
        public boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
          anchorNodeDelete = anchorNode;
          WritableMap map = Arguments.createMap();
          map.putString("ID", "Name");
          ModuleWithEmitter.sendEvent(context, ModuleWithEmitter.EMIT_GET_NAME, map);
          return true;
        }
      });
    });
  }

  public void setSCALE(Float SCALE) {
    this.SCALE = SCALE;
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public boolean setObject(String uriString) {
    AtomicBoolean loadComplete = new AtomicBoolean(true);
    Uri uri = Uri.fromFile(new File(uriString));
    ModelRenderable.builder()
      .setSource(reactActivity, uri)
      .build()
      .thenAccept(modelRenderable -> objectRender = modelRenderable)
      .exceptionally(
        throwable -> {
          Toast toast =
            Toast.makeText(reactActivity, "Cant't load object, because:" + throwable.toString(), Toast.LENGTH_LONG);
          toast.setGravity(Gravity.CENTER, 0, 0);
          toast.show();
          loadComplete.set(false);
          return null;
        });
    setIdItem(uriString.split("/")[uriString.split("/").length - 1]);
    return loadComplete.get();
  }

  @SuppressLint("SetTextI18n")
  public void calcDistanceToObject() {
    Frame frame = arFragment.getArSceneView().getArFrame();
    Pose camera = frame.getCamera().getPose();
    Vector3 objectMatrix = anchorNodeSelected.getWorldPosition();
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

  public void deleteNodeObject(Boolean delete) {
    if (delete) {
      arFragment.getArSceneView().getScene().removeChild(anchorNodeDelete);
      anchorNodeDelete.getAnchor().detach();
      anchorNodeDelete.setParent(null);
      anchorNodeDelete = null;
    } else {

    }

  }


  public static ReactActivity getActivity() {
    return reactActivity;
  }

  public String getIdItem() {
    return idItem;
  }

  public void setIdItem(String item) {
    this.idItem = item;
  }

}
