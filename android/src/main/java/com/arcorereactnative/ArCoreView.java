package com.arcorereactnative;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.ReactActivity;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

public class ArCoreView extends FrameLayout {
  public static ReactActivity reactActivity = null;
  private ThemedReactContext context;
  private ArFragment arFragment;
  private ModelRenderable objectRender;
  private Float SCALE = 0.1f;
  private AnchorNode anchorNodeDelete;
  private AnchorNode anchorNodeSelected;
  private LinearLayout bottomSheetLayout;
  private BottomSheetBehavior<LinearLayout> sheetBehavior;
  protected ImageView bottomSheetArrowImageView;
  private LinearLayout gestureLayout;
  private Spinner modelSpinner;
  private TextView calcInformation;

  public static void setContext(ReactActivity context) {
    reactActivity = context;
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public ArCoreView(ThemedReactContext context) {
    super(context);
    this.context = context;
    init();
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void init() {
    inflate(reactActivity, R.layout.activity_main, this);
    arFragment = (ArFragment) reactActivity.getSupportFragmentManager().findFragmentById(R.id.ui_fragment);
    Uri uri = Uri.parse("chair2.sfb");
    Log.e("URI", uri.toString());
    ModelRenderable.builder()
      .setSource(reactActivity, uri)
      .build()
      .thenAccept(modelRenderable -> objectRender = modelRenderable)
      .exceptionally(
        throwable -> {
          Toast toast =
            Toast.makeText(reactActivity, "Unable to load andy renderable", Toast.LENGTH_LONG);
          toast.setGravity(Gravity.CENTER, 0, 0);
          toast.show();
          return null;
        }
      );
    arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
      if (objectRender == null) {
        Log.e("LOI", "ANH NULL");
        return;
      }
      Anchor anchor = hitResult.createAnchor();
      AnchorNode anchorNode = new AnchorNode(anchor);
      anchorNode.setParent(arFragment.getArSceneView().getScene());

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
          return true;
        }
      });
    });
    bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
    sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
    gestureLayout = findViewById(R.id.gesture_layout);
    bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);
    modelSpinner = findViewById(R.id.model_spinner);
    calcInformation = findViewById(R.id.distance_information);
    ViewTreeObserver vto = getViewTreeObserver();
    vto.addOnGlobalLayoutListener(
      new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
          if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
          } else {
            gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          }
          //                int width = bottomSheetLayout.getMeasuredWidth();
          int height = gestureLayout.getMeasuredHeight();

          sheetBehavior.setPeekHeight(height);
        }
      });
    sheetBehavior.setHideable(false);
    sheetBehavior.setBottomSheetCallback(
      new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
          switch (newState) {
            case BottomSheetBehavior.STATE_HIDDEN:
              break;
            case BottomSheetBehavior.STATE_EXPANDED: {
              bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
            }
            break;
            case BottomSheetBehavior.STATE_COLLAPSED: {
              bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
            }
            break;
            case BottomSheetBehavior.STATE_DRAGGING:
              break;
            case BottomSheetBehavior.STATE_SETTLING:
              bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
              break;
          }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
      });
    String[] arrayProduct = new String[]{
      "chair", "chair2"};
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(reactActivity,
      android.R.layout.simple_spinner_dropdown_item, arrayProduct);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    modelSpinner.setAdapter(adapter);
    modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);
        if (item != null) {
          Uri uri = Uri.parse(item.toString() + ".sfb");
          ModelRenderable.builder()
            .setSource(reactActivity, uri)
            .build()
            .thenAccept(modelRenderable -> objectRender = modelRenderable)
            .exceptionally(
              throwable -> {
                Toast toast =
                  Toast.makeText(reactActivity, "Unable to load andy renderable:"+throwable.toString(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return null;
              });
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });
    calcInformation.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        calcDistanceToObject();
      }
    });
  }

  public void setSCALE(Float SCALE) {
    this.SCALE = SCALE;
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void changeObject(String uriString) {
    Uri uri = Uri.parse(uriString);
    ModelRenderable.builder()
      .setSource(reactActivity, uri)
      .build()
      .thenAccept(modelRenderable -> objectRender = modelRenderable)
      .exceptionally(
        throwable -> {
          Toast toast =
            Toast.makeText(reactActivity, "Unable to load andy renderable:"+throwable.toString(), Toast.LENGTH_LONG);
          toast.setGravity(Gravity.CENTER, 0, 0);
          toast.show();
          return null;
        });
  }

  @SuppressLint("SetTextI18n")
  public void calcDistanceToObject() {
    Frame frame = arFragment.getArSceneView().getArFrame();
    Pose camera = frame.getCamera().getPose();
    Vector3 objectMatrix = anchorNodeSelected.getWorldPosition();
    Double distance = calculateDistance(objectMatrix, camera);
    calcInformation.setText(String.valueOf(distance * 100) + " cm");

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

  public void deleteNodeObject() {
    arFragment.getArSceneView().getScene().removeChild(anchorNodeDelete);
    anchorNodeDelete.getAnchor().detach();
    anchorNodeDelete.setParent(null);
    anchorNodeDelete = null;
  }


  public static ReactActivity getActivity() {
    return reactActivity;
  }

}
