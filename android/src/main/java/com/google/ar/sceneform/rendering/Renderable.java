package com.google.ar.sceneform.rendering;

import android.content.Context;
import android.net.Uri;
import android.os.Build;


import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.collision.CollisionShape;
import com.google.ar.sceneform.common.TransformProvider;
import com.google.ar.sceneform.math.Matrix;
import com.google.ar.sceneform.resources.ResourceRegistry;
import com.google.ar.sceneform.utilities.AndroidPreconditions;
import com.google.ar.sceneform.utilities.ChangeId;
import com.google.ar.sceneform.utilities.LoadHelper;
import com.google.ar.sceneform.utilities.Preconditions;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Base class for rendering in 3D space by attaching to a {@link com.google.ar.sceneform.Node} with
 * {@link com.google.ar.sceneform.Node#setRenderable(Renderable)}.
 */
@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"}) // CompletableFuture
public abstract class Renderable {
  // Data that can be shared between Renderables with makeCopy()
  private final IRenderableInternalData renderableData;

  // Data that is unique per-Renderable.
  private final ArrayList<Material> materialBindings = new ArrayList<>();
  private final ArrayList<String> materialNames = new ArrayList<>();
  private int renderPriority = RENDER_PRIORITY_DEFAULT;
  private boolean isShadowCaster = true;
  private boolean isShadowReceiver = true;
  @Nullable
  protected CollisionShape collisionShape;

  private final ChangeId changeId = new ChangeId();

  public static final int RENDER_PRIORITY_DEFAULT = 4;
  public static final int RENDER_PRIORITY_FIRST = 0;
  public static final int RENDER_PRIORITY_LAST = 7;
  // Allow stale data two weeks old by default.
  private static final long DEFAULT_MAX_STALE_CACHE = TimeUnit.DAYS.toSeconds(14);

  /** @hide */
  @RequiresApi(api = Build.VERSION_CODES.N)
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  protected Renderable(Renderable.Builder<? extends Renderable, ? extends Builder<?, ?>> builder) {
    Preconditions.checkNotNull(builder, "Parameter \"builder\" was null.");
    if (builder.isFilamentAsset) {
      renderableData = new RenderableInternalFilamentAssetData();
    } else if (builder.isGltf) {
      renderableData = createRenderableInternalGltfData();
    } else {
      renderableData = new RenderableInternalData();
    }
    if (builder.definition != null) {
      updateFromDefinition(builder.definition);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @SuppressWarnings("initialization")
  protected Renderable(Renderable other) {
    if (other.getId().isEmpty()) {
      throw new AssertionError("Cannot copy uninitialized Renderable.");
    }

    // Share renderableData with the original Renderable.
    renderableData = other.renderableData;

    // Copy materials.
    Preconditions.checkState(other.materialNames.size() == other.materialBindings.size());
    for (int i = 0; i < other.materialBindings.size(); i++) {
      Material otherMaterial = other.materialBindings.get(i);
      materialBindings.add(otherMaterial.makeCopy());
      materialNames.add(other.materialNames.get(i));
    }

    renderPriority = other.renderPriority;
    isShadowCaster = other.isShadowCaster;
    isShadowReceiver = other.isShadowReceiver;

    // Copy collision shape.
    if (other.collisionShape != null) {
      collisionShape = other.collisionShape.makeCopy();
    }

    changeId.update();
  }

  /** Get the {@link CollisionShape} used for collision detection with this {@link Renderable}. */
  public @Nullable CollisionShape getCollisionShape() {
    return collisionShape;
  }

  /** Set the {@link CollisionShape} used for collision detection with this {@link Renderable}. */
  public void setCollisionShape(@Nullable CollisionShape collisionShape) {
    this.collisionShape = collisionShape;
    changeId.update();
  }

  /** Returns the material bound to the first submesh. */
  public Material getMaterial() {
    return getMaterial(0);
  }

  /** Returns the material bound to the specified submesh. */
  public Material getMaterial(int submeshIndex) {
    if (submeshIndex < materialBindings.size()) {
      return materialBindings.get(submeshIndex);
    }

    throw makeSubmeshOutOfRangeException(submeshIndex);
  }

  /** Sets the material bound to the first submesh. */
  public void setMaterial(Material material) {
    setMaterial(0, material);
  }

  /** Sets the material bound to the specified submesh. */
  public void setMaterial(int submeshIndex, Material material) {
    if (submeshIndex < materialBindings.size()) {
      materialBindings.set(submeshIndex, material);
      changeId.update();
    } else {
      throw makeSubmeshOutOfRangeException(submeshIndex);
    }
  }

  /**
   * Returns the name associated with the specified submesh.
   *
   * @throws IllegalArgumentException if the index is out of range
   */
  public String getSubmeshName(int submeshIndex) {
    Preconditions.checkState(materialNames.size() == materialBindings.size());
    if (submeshIndex >= 0 && submeshIndex < materialNames.size()) {
      return materialNames.get(submeshIndex);
    }

    throw makeSubmeshOutOfRangeException(submeshIndex);
  }

  /**
   * Get the render priority that controls the order of rendering. The priority is between a range
   * of 0 (rendered first) and 7 (rendered last). The default value is 4.
   */
  public int getRenderPriority() {
    return renderPriority;
  }

  /**
   * Set the render priority to control the order of rendering. The priority is between a range of 0
   * (rendered first) and 7 (rendered last). The default value is 4.
   */
  public void setRenderPriority(
      @IntRange(from = RENDER_PRIORITY_FIRST, to = RENDER_PRIORITY_LAST) int renderPriority) {
    this.renderPriority =
        Math.min(RENDER_PRIORITY_LAST, Math.max(RENDER_PRIORITY_FIRST, renderPriority));
    changeId.update();
  }

  /** Returns true if configured to cast shadows on other renderables. */
  public boolean isShadowCaster() {
    return isShadowCaster;
  }

  /** Sets whether the renderable casts shadow on other renderables in the scene. */
  public void setShadowCaster(boolean isShadowCaster) {
    this.isShadowCaster = isShadowCaster;
    changeId.update();
  }

  /** Returns true if configured to receive shadows cast by other renderables. */
  public boolean isShadowReceiver() {
    return isShadowReceiver;
  }

  /** Sets whether the renderable receives shadows cast by other renderables in the scene. */
  public void setShadowReceiver(boolean isShadowReceiver) {
    this.isShadowReceiver = isShadowReceiver;
    changeId.update();
  }

  /**
   * Returns the number of submeshes that this renderable has. All Renderables have at least one.
   */
  public int getSubmeshCount() {
    return renderableData.getMeshes().size();
  }

  /** @hide */
  public ChangeId getId() {
    return changeId;
  }

  /** @hide */
  @RequiresApi(api = Build.VERSION_CODES.N)
  public RenderableInstance createInstance(TransformProvider transformProvider) {
    return new RenderableInstance(transformProvider, this);
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void updateFromDefinition(RenderableDefinition definition) {
    Preconditions.checkState(!definition.getSubmeshes().isEmpty());

    changeId.update();

    definition.applyDefinitionToData(renderableData, materialBindings, materialNames);

    collisionShape = new Box(renderableData.getSizeAabb(), renderableData.getCenterAabb());
  }

  /**
   * Creates a new instance of this Renderable.
   *
   * <p>The new renderable will have unique copy of all mutable state. All materials referenced by
   * the Renderable will also be instanced. Immutable data will be shared between the instances.
   */
  public abstract Renderable makeCopy();

  IRenderableInternalData getRenderableData() {
    return renderableData;
  }

  ArrayList<Material> getMaterialBindings() {
    return materialBindings;
  }

  ArrayList<String> getMaterialNames() {
    return materialNames;
  }

  /**
   * Optionally override in subclasses for work that must be done each frame for specific types of
   * Renderables. For example, ViewRenderable uses this to prevent the renderable from being visible
   * until the view has been successfully drawn to an external texture, and initializing material
   * parameters.
   */
  void prepareForDraw() {}

  void attachToRenderer(Renderer renderer) {}

  void detatchFromRenderer() {}

  /**
   * Gets the final model matrix to use for rendering this {@link Renderable} based on the matrix
   * passed in. Default implementation simply passes through the original matrix. WARNING: Do not
   * modify the originalMatrix! If the final matrix isn't the same as the original matrix, then a
   * new instance must be returned.
   *
   * @hide
   */
  public Matrix getFinalModelMatrix(final Matrix originalMatrix) {
    Preconditions.checkNotNull(originalMatrix, "Parameter \"originalMatrix\" was null.");
    return originalMatrix;
  }

  private IllegalArgumentException makeSubmeshOutOfRangeException(int submeshIndex) {
    return new IllegalArgumentException(
        "submeshIndex ("
            + submeshIndex
            + ") is out of range. It must be less than the submeshCount ("
            + getSubmeshCount()
            + ").");
  }


  private IRenderableInternalData createRenderableInternalGltfData() {return null;}



  // TODO: Gltf animation api should be consistent with Sceneform.





  // TODO: Gltf animation api should be consistent with Sceneform.






  /**
   * Used to programmatically construct a {@link Renderable}. Builder data is stored, not copied. Be
   * careful when modifying the data before or between build calls.
   */
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"}) // CompletableFuture
  abstract static class Builder<T extends Renderable, B extends Builder<T, B>> {
    /** @hide */
    @Nullable protected Object registryId = null;
    /** @hide */
    @Nullable protected Context context = null;

    @Nullable private Uri sourceUri = null;
    @Nullable private Callable<InputStream> inputStreamCreator = null;
    @Nullable private RenderableDefinition definition = null;
    private boolean isGltf = false;
    private boolean isFilamentAsset = false;
    @Nullable private LoadGltfListener loadGltfListener;
    @Nullable private Function<String, Uri> uriResolver = null;
    @Nullable private byte[] materialsBytes = null;

    /** Used to programmatically construct a {@link Renderable}. */
    protected Builder() {}

    public B setSource(Context context, Callable<InputStream> inputStreamCreator) {
      Preconditions.checkNotNull(inputStreamCreator);
      this.sourceUri = null;
      this.inputStreamCreator = inputStreamCreator;
      this.context = context;
      return getSelf();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public B setSource(Context context, Uri sourceUri) {
      return setRemoteSourceHelper(context, sourceUri, true);
    }


    public B setSource(Context context, Uri sourceUri, boolean enableCaching) {return null;}



    public B setSource(Context context, int resource) {
      this.inputStreamCreator = LoadHelper.fromResource(context, resource);
      this.context = context;

      Uri uri = LoadHelper.resourceToUri(context, resource);
      this.sourceUri = uri;
      this.registryId = uri;
      return getSelf();
    }

    /** Build a {@link Renderable} from a {@link RenderableDefinition}. */
    public B setSource(RenderableDefinition definition) {
      this.definition = definition;
      registryId = null;
      sourceUri = null;
      return getSelf();
    }

    public B setRegistryId(@Nullable Object registryId) {
      this.registryId = registryId;
      return getSelf();
    }









    public B setIsFilamentGltf(boolean isFilamentGltf) {
      this.isFilamentAsset = isFilamentGltf;
      return getSelf();
    }







    /**
     * True if a source function will be called during build
     *
     * @hide
     */
    public Boolean hasSource() {
      return sourceUri != null || inputStreamCreator != null || definition != null;
    }

    /**
     * Constructs a {@link Renderable} with the parameters of the builder.
     *
     * @return the constructed {@link Renderable}
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<T> build() {
      try {
        checkPreconditions();
      } catch (Throwable failedPrecondition) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(failedPrecondition);
        FutureHelper.logOnException(
            getRenderableClass().getSimpleName(),
            result,
            "Unable to load Renderable registryId='" + registryId + "'");
        return result;
      }

      // For static-analysis check.
      Object registryId = this.registryId;
      if (registryId != null) {
        // See if a renderable has already been registered by this id, if so re-use it.
        ResourceRegistry<T> registry = getRenderableRegistry();
        CompletableFuture<T> renderableFuture = registry.get(registryId);
        if (renderableFuture != null) {
          return renderableFuture.thenApply(
              renderable -> getRenderableClass().cast(renderable.makeCopy()));
        }
      }

      T renderable = makeRenderable();

      if (definition != null) {
        return CompletableFuture.completedFuture(renderable);
      }

      // For static-analysis check.
      Callable<InputStream> inputStreamCreator = this.inputStreamCreator;
      if (inputStreamCreator == null) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(new AssertionError("Input Stream Creator is null."));
        FutureHelper.logOnException(
            getRenderableClass().getSimpleName(),
            result,
            "Unable to load Renderable registryId='" + registryId + "'");
        return result;
      }

      CompletableFuture<T> result = null;
      if (isFilamentAsset) {
        if (context != null) {
          result = loadRenderableFromFilamentGltf(context, renderable);
        } else {
          throw new AssertionError("Gltf Renderable.Builder must have a valid context.");
        }
      } else if (isGltf) {
        if (context != null) {
          result = loadRenderableFromGltf(context, renderable, this.materialsBytes);
        } else {
          throw new AssertionError("Gltf Renderable.Builder must have a valid context.");
        }
      } else {
        LoadRenderableFromSfbTask<T> loader =
            new LoadRenderableFromSfbTask<>(renderable, sourceUri);
        result = loader.downloadAndProcessRenderable(inputStreamCreator);
      }

      if (registryId != null) {
        ResourceRegistry<T> registry = getRenderableRegistry();
        registry.register(registryId, result);
      }

      FutureHelper.logOnException(
          getRenderableClass().getSimpleName(),
          result,
          "Unable to load Renderable registryId='" + registryId + "'");
      return result.thenApply(
          resultRenderable -> getRenderableClass().cast(resultRenderable.makeCopy()));
    }

    protected void checkPreconditions() {
      AndroidPreconditions.checkUiThread();

      if (!hasSource()) {
        throw new AssertionError("ModelRenderable must have a source.");
      }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private B setRemoteSourceHelper(Context context, Uri sourceUri, boolean enableCaching) {
      Preconditions.checkNotNull(sourceUri);
      this.sourceUri = sourceUri;
      this.context = context;
      this.registryId = sourceUri;
      // Configure caching.
      if (enableCaching) {
        this.setCachingEnabled(context);
      }

      Map<String, String> connectionProperties = new HashMap<>();
      if (!enableCaching) {
        connectionProperties.put("Cache-Control", "no-cache");
      } else {
        connectionProperties.put("Cache-Control", "max-stale=" + DEFAULT_MAX_STALE_CACHE);
      }
      this.inputStreamCreator =
          LoadHelper.fromUri(
              context, Preconditions.checkNotNull(this.sourceUri), connectionProperties);
      return getSelf();
    }


    private CompletableFuture<T> loadRenderableFromGltf(
      @NonNull Context context, T renderable, @Nullable byte[] materialsBytes) {return null;}






    @RequiresApi(api = Build.VERSION_CODES.N)
    private CompletableFuture<T> loadRenderableFromFilamentGltf(
        @NonNull Context context, T renderable) {
      LoadRenderableFromFilamentGltfTask<T> loader =
          new LoadRenderableFromFilamentGltfTask<>(
              renderable, context, Preconditions.checkNotNull(sourceUri), uriResolver);
      return loader.downloadAndProcessRenderable(Preconditions.checkNotNull(inputStreamCreator));
    }


    private void setCachingEnabled(Context context) {return ;}



    protected abstract T makeRenderable();

    protected abstract Class<T> getRenderableClass();

    protected abstract ResourceRegistry<T> getRenderableRegistry();

    protected abstract B getSelf();
  }
}
