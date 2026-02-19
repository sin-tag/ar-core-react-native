// create by hoangtuyensk@gmail.com - github: sig-tag
package com.arcorereactnative;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;


public class ArCoreModuleViewManager extends ViewGroupManager<ArCoreView> {

    public static final int COMMAND_ADD_OBJECT = 1;
    public static final int COMMAND_REMOVE_SELECTED_OBJECT = 2;
    public static final int COMMAND_TAKE_SCREENSHOT = 3;
    public static final int COMMAND_SET_DUPLICATE = 4;
    public static final int COMMAND_RESET = 5;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArCoreModuleViewManager(ReactApplicationContext reactContext) {
        super();
    }

    @NonNull
    @Override
    public String getName() {
        return "ArCoreReactNativeView";
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public ArCoreView createViewInstance(ThemedReactContext reactContext) {
        return new ArCoreView(reactContext);
    }

    // -- Declarative props --

    @ReactProp(name = "planeDetection")
    public void setPlaneDetection(ArCoreView view, @Nullable String mode) {
        // Plane detection is configured at session level on Android;
        // stored for potential future session reconfiguration
    }

    @ReactProp(name = "lightEstimation", defaultBoolean = true)
    public void setLightEstimation(ArCoreView view, boolean enabled) {
        // Light estimation setting
    }

    @ReactProp(name = "showPlanes", defaultBoolean = false)
    public void setShowPlanes(ArCoreView view, boolean show) {
        // Show/hide plane visualization
    }

    @ReactProp(name = "showFeaturePoints", defaultBoolean = false)
    public void setShowFeaturePoints(ArCoreView view, boolean show) {
        // Show/hide feature points
    }

    // -- Imperative commands --

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.<String, Integer>builder()
                .put("addObject", COMMAND_ADD_OBJECT)
                .put("removeSelectedObject", COMMAND_REMOVE_SELECTED_OBJECT)
                .put("takeScreenshot", COMMAND_TAKE_SCREENSHOT)
                .put("setDuplicate", COMMAND_SET_DUPLICATE)
                .put("reset", COMMAND_RESET)
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void receiveCommand(@NonNull ArCoreView root, String commandId, @Nullable ReadableArray args) {
        super.receiveCommand(root, commandId, args);
        switch (commandId) {
            case "addObject":
                if (args != null && args.size() >= 2) {
                    String modelPath = args.getString(0);
                    String id = args.getString(1);
                    root.setIdItem(id);
                    root.setObject(modelPath);
                }
                break;
            case "removeSelectedObject":
                root.deleteNodeObject();
                break;
            case "takeScreenshot":
                root.screenShort();
                break;
            case "setDuplicate":
                root.setMultiObject();
                break;
            case "reset":
                root.killProcess();
                break;
            default:
                break;
        }
    }

    // -- Events --

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        builder.put("onObjectSelected",
                MapBuilder.of("registrationName", "onObjectSelected"));
        builder.put("onScreenshot",
                MapBuilder.of("registrationName", "onScreenshot"));
        builder.put("onPlaneDetected",
                MapBuilder.of("registrationName", "onPlaneDetected"));
        builder.put("onTrackingStateChange",
                MapBuilder.of("registrationName", "onTrackingStateChange"));
        return builder.build();
    }
}
