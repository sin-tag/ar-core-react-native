package com.reactnativeawesomemodule

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class AwesomeModuleViewManager(reactContext: ReactApplicationContext) : SimpleViewManager<ArCoreView>() {
  var arCoreView: ArCoreView = TODO();
  override fun getName() = "AwesomeModuleView"

  @RequiresApi(Build.VERSION_CODES.N)
  public override fun createViewInstance(reactContext: ThemedReactContext): ArCoreView {
    arCoreView = ArCoreView(reactContext);
    return arCoreView;
  }

  public fun getViewInstance(): ArCoreView {
    return arCoreView;
  }

}
