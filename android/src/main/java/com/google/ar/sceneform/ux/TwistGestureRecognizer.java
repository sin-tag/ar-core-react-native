/*
 * Copyright 2018 Google LLC All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.ux;

import android.view.MotionEvent;
import com.google.ar.sceneform.HitTestResult;

/** Gesture Recognizer for when the user performs a two-finger twist motion on the touch screen. */
public class TwistGestureRecognizer extends BaseGestureRecognizer<TwistGesture> {
  /** Interface definition for a callbacks to be invoked when a {@link TwistGesture} starts. */
  public interface OnGestureStartedListener
      extends BaseGestureRecognizer.OnGestureStartedListener<TwistGesture> {}

  public TwistGestureRecognizer(GesturePointersUtility gesturePointersUtility) {
    super(gesturePointersUtility);
  }

  @Override
  protected void tryCreateGestures(HitTestResult hitTestResult, MotionEvent motionEvent) {
    // Twist gestures require at least two fingers to be touching.
    if (motionEvent.getPointerCount() < 2) {
      return;
    }

    int actionId = motionEvent.getPointerId(motionEvent.getActionIndex());
    int action = motionEvent.getActionMasked();
    boolean touchBegan =
        action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN;

    if (!touchBegan || gesturePointersUtility.isPointerIdRetained(actionId)) {
      return;
    }

    // Determine if there is another pointer Id that has not yet been retained.
    for (int i = 0; i < motionEvent.getPointerCount(); i++) {
      int pointerId = motionEvent.getPointerId(i);
      if (pointerId == actionId) {
        continue;
      }

      if (gesturePointersUtility.isPointerIdRetained(pointerId)) {
        continue;
      }

      gestures.add(new TwistGesture(gesturePointersUtility, motionEvent, pointerId));
    }
  }
}
