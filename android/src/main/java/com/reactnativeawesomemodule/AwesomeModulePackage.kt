package com.reactnativeawesomemodule

import android.os.Build
import androidx.annotation.RequiresApi
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager


class AwesomeModulePackage : ReactPackage {
  private var arCoreViewManager: AwesomeModuleViewManager = TODO()
  @RequiresApi(Build.VERSION_CODES.N)
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return listOf(ArCoreViewModule(reactContext))
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    arCoreViewManager = AwesomeModuleViewManager(reactContext);
    return listOf(arCoreViewManager)
  }
}
