package com.example.reactnativeawesomemodule;

import android.os.Bundle;

import com.facebook.react.ReactActivity;
import com.arcorereactnative.ArCoreView;

public class MainActivity extends ReactActivity {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "ArCoreViewModule";
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }
}
