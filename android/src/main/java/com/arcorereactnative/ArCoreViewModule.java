// create by hoangtuyensk@gmail.com - github: sig-tag
package com.arcorereactnative;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

public class ArCoreViewModule extends ReactContextBaseJavaModule {

  @RequiresApi(api = Build.VERSION_CODES.N)
  ArCoreViewModule(ReactApplicationContext context) {
    super(context);

  }

  @Override
  public String getName() {
    return "ArCoreViewModule";
  }


}
