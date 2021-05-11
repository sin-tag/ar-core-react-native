// create by hoangtuyensk@gmail.com - github: sig-tag
package com.arcorereactnative;

import android.app.Activity;
import android.os.Build;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import com.facebook.react.uimanager.ThemedReactContext;

public class ArCoreViewMain extends LinearLayout {
  private Activity mActivity;
  private ArCoreView arCoreView;

  @RequiresApi(api = Build.VERSION_CODES.N)
  public ArCoreViewMain(ThemedReactContext context, Activity activity) {
    super(context);
    mActivity = activity;
    arCoreView = new ArCoreView(context);
  }
}
