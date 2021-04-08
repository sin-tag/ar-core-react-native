package com.example.reactnativeawesomemodule;

import android.os.Bundle;

import com.facebook.react.ReactActivity;
import com.reactnativeawesomemodule.ArCoreView;

public class MainActivity extends ReactActivity {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "AwesomeModuleExample";
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ArCoreView.setContext(this);
  }
}
