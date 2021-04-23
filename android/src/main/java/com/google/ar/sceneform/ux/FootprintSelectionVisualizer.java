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

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;

/**
 * Visualizes that a {@link BaseTransformableNode} is selected by rendering a footprint for the
 * selected node.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class FootprintSelectionVisualizer implements SelectionVisualizer {
  private final Node footprintNode;
  @Nullable
  private ModelRenderable footprintRenderable;

  public FootprintSelectionVisualizer() {
    footprintNode = new Node();
  }

  public void setFootprintRenderable(ModelRenderable renderable) {
    ModelRenderable copyRenderable = renderable.makeCopy();
    footprintNode.setRenderable(copyRenderable);
    copyRenderable.setCollisionShape(null);
    footprintRenderable = copyRenderable;
  }

  @Nullable
  public ModelRenderable getFootprintRenderable() {
    return footprintRenderable;
  }

  @Override
  public void applySelectionVisual(BaseTransformableNode node) {
    footprintNode.setParent(node);
  }

  @Override
  public void removeSelectionVisual(BaseTransformableNode node) {
    footprintNode.setParent(null);
  }
}
