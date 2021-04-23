package com.google.ar.sceneform;

import android.os.Build;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.ar.sceneform.Scene.OnPeekTouchListener;
import com.google.ar.sceneform.utilities.Preconditions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Manages propagation of touch events to node's within a scene.
 *
 * <p>The way that touch events are propagated mirrors the way touches are propagated to Android
 * Views.
 *
 * <p>When an ACTION_DOWN event occurs, that represents that start of a gesture. ACTION_UP or
 * ACTION_CANCEL represents when a gesture ends. When a gesture starts, the following is done:
 *
 * <ul>
 *   <li>Call {@link Node#dispatchTouchEvent(HitTestResult, MotionEvent)} on the node that was
 *       touched as detected by scene.hitTest.
 *   <li>If {@link Node#dispatchTouchEvent(HitTestResult, MotionEvent)} returns false, recurse
 *       upwards through the node's parents and call {@link Node#dispatchTouchEvent(HitTestResult,
 *       MotionEvent)} until one of the node's returns true.
 *   <li>If every node returns false, the gesture is ignored and subsequent events that are part of
 *       the gesture will not be passed to any nodes.
 *   <li>If one of the node's returns true, then that node will receive all future touch events for
 *       the gesture.
 * </ul>
 *
 * @hide
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class TouchEventSystem {
  private Method motionEventSplitMethod;
  private final Object[] motionEventSplitParams = new Object[1];

  /**
   * Keeps track of which nodes are handling events for which pointer Id's. Implemented as a linked
   * list to store an ordered list of touch targets.
   */
  private static class TouchTarget {
    public static final int ALL_POINTER_IDS = -1; // all ones

    // The touch target.
    public Node node;

    // The combined bit mask of pointer ids for all pointers captured by the target.
    public int pointerIdBits;

    // The next target in the target list.
    @Nullable
    public TouchTarget next;
  }

  @Nullable private Scene.OnTouchListener onTouchListener;
  private final ArrayList<OnPeekTouchListener> onPeekTouchListeners = new ArrayList<>();

  // The touch listener that is handling the current gesture.
  @Nullable private Scene.OnTouchListener handlingTouchListener = null;

  // Linked list of nodes that are currently handling touches for a set of pointers.
  @Nullable private TouchTarget firstHandlingTouchTarget = null;

  public TouchEventSystem() {}

  /**
   * Get the currently registered callback for touch events.
   *
   * @see #setOnTouchListener(Scene.OnTouchListener)
   * @return the attached touch listener
   */
  @Nullable
  public Scene.OnTouchListener getOnTouchListener() {
    return onTouchListener;
  }

  /**
   * Register a callback to be invoked when the scene is touched. The callback is invoked before any
   * node receives the event. If the callback handles the event, then the gesture is never received
   * by the nodes.
   *
   * @param onTouchListener the touch listener to attach
   */
  public void setOnTouchListener(@Nullable Scene.OnTouchListener onTouchListener) {
    this.onTouchListener = onTouchListener;
  }

  /**
   * Adds a listener that will be called before the {@link Scene.OnTouchListener} is invoked. This
   * is invoked even if the gesture was consumed, making it possible to observe all motion events
   * dispatched to the scene. This is called even if the touch is not over a node, in which case
   * {@link HitTestResult#getNode()} will be null. The listeners will be called in the order in
   * which they were added.
   *
   * @param onPeekTouchListener the peek touch listener to add
   */
  public void addOnPeekTouchListener(OnPeekTouchListener onPeekTouchListener) {
    if (!onPeekTouchListeners.contains(onPeekTouchListener)) {
      onPeekTouchListeners.add(onPeekTouchListener);
    }
  }

  /**
   * Removes a listener that will be called before the {@link Scene.OnTouchListener} is invoked.
   * This is invoked even if the gesture was consumed, making it possible to observe all motion
   * events dispatched to the scene. This is called even if the touch is not over a node, in which
   * case {@link HitTestResult#getNode()} will be null.
   *
   * @param onPeekTouchListener the peek touch listener to remove
   */
  public void removeOnPeekTouchListener(OnPeekTouchListener onPeekTouchListener) {
    onPeekTouchListeners.remove(onPeekTouchListener);
  }

  public void onTouchEvent(HitTestResult hitTestResult, MotionEvent motionEvent) {
    Preconditions.checkNotNull(hitTestResult, "Parameter \"hitTestResult\" was null.");
    Preconditions.checkNotNull(motionEvent, "Parameter \"motionEvent\" was null.");

    int actionMasked = motionEvent.getActionMasked();

    // This is a brand new gesture, so clear everything.
    if (actionMasked == MotionEvent.ACTION_DOWN) {
      clearTouchTargets();
    }

    // Dispatch touch event to the peek touch listener, which reveives all events even if the
    // gesture is being consumed.
    for (OnPeekTouchListener onPeekTouchListener : onPeekTouchListeners) {
      onPeekTouchListener.onPeekTouch(hitTestResult, motionEvent);
    }

    // If the touch listener is already handling the gesture, always dispatch to it.
    if (handlingTouchListener != null) {
      tryDispatchToSceneTouchListener(hitTestResult, motionEvent);
    } else {

      TouchTarget newTouchTarget = null;
      boolean alreadyDispatchedToNewTouchTarget = false;
      boolean alreadyDispatchedToAnyTarget = false;
      Node hitNode = hitTestResult.getNode();

      // New pointer has touched the scene.
      // Find the appropriate touch target for this pointer.
      if ((actionMasked == MotionEvent.ACTION_DOWN
          || (actionMasked == MotionEvent.ACTION_POINTER_DOWN))) {
        int actionIndex = motionEvent.getActionIndex();
        int idBitsToAssign = 1 << motionEvent.getPointerId(actionIndex);

        // Clean up earlier touch targets for this pointer id in case they have become out of sync.
        removePointersFromTouchTargets(idBitsToAssign);

        // See if this event occurred on a node that is already a touch target.
        if (hitNode != null) {
          newTouchTarget = getTouchTargetForNode(hitNode);
          if (newTouchTarget != null) {
            // Give the existing touch target the new pointer in addition to the one it is handling.
            newTouchTarget.pointerIdBits |= idBitsToAssign;
          } else {
            Node handlingNode =
                dispatchTouchEvent(motionEvent, hitTestResult, hitNode, idBitsToAssign, true);
            if (handlingNode != null) {
              newTouchTarget = addTouchTarget(handlingNode, idBitsToAssign);
              alreadyDispatchedToNewTouchTarget = true;
            }
            alreadyDispatchedToAnyTarget = true;
          }
        }

        if (newTouchTarget == null && firstHandlingTouchTarget != null) {
          // did not find an existing target to receive the event.
          // Assign the pointer to the least recently added target.
          newTouchTarget = firstHandlingTouchTarget;
          while (newTouchTarget.next != null) {
            newTouchTarget = newTouchTarget.next;
          }
          newTouchTarget.pointerIdBits |= idBitsToAssign;
        }
      }

      // Dispatch event to touch targets.
      if (firstHandlingTouchTarget != null) {
        TouchTarget target = firstHandlingTouchTarget;
        while (target != null) {
          TouchTarget next = target.next;
          if (!alreadyDispatchedToNewTouchTarget || target != newTouchTarget) {
            dispatchTouchEvent(
                motionEvent, hitTestResult, target.node, target.pointerIdBits, false);
          }
          target = next;
        }
      } else if (!alreadyDispatchedToAnyTarget) {
        tryDispatchToSceneTouchListener(hitTestResult, motionEvent);
      }
    }

    if (actionMasked == MotionEvent.ACTION_CANCEL || actionMasked == MotionEvent.ACTION_UP) {
      clearTouchTargets();
    } else if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
      int actionIndex = motionEvent.getActionIndex();
      int idBitsToRemove = 1 << motionEvent.getPointerId(actionIndex);
      removePointersFromTouchTargets(idBitsToRemove);
    }
  }

  private boolean tryDispatchToSceneTouchListener(
      HitTestResult hitTestResult, MotionEvent motionEvent) {
    // This is a new gesture, give the touch listener a chance to capture the input.
    if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
      // If the listener handles the gesture, then the event is never propagated to the nodes.
      if (onTouchListener != null && onTouchListener.onSceneTouch(hitTestResult, motionEvent)) {
        // The touch listener is handling the gesture, return early.
        handlingTouchListener = onTouchListener;
        return true;
      }
    } else if (handlingTouchListener != null) {
      handlingTouchListener.onSceneTouch(hitTestResult, motionEvent);
      return true;
    }

    return false;
  }
  @RequiresApi(api = Build.VERSION_CODES.N)
  private MotionEvent splitMotionEvent(MotionEvent motionEvent, int idBits) {
    if (motionEventSplitMethod == null) {
      try {
        Class<MotionEvent> motionEventClass = MotionEvent.class;
        motionEventSplitMethod = motionEventClass.getMethod("split", int.class);
      } catch (ReflectiveOperationException ex) {
        throw new RuntimeException("Splitting MotionEvent not supported.", ex);
      }
    }

    try {
      motionEventSplitParams[0] = idBits;
      Object result = motionEventSplitMethod.invoke(motionEvent, motionEventSplitParams);
      // MotionEvent.split is guaranteed to return a NonNull result, but the null check is required
      // for static analysis.
      if (result != null) {
        return (MotionEvent) result;
      } else {
        return motionEvent;
      }
    } catch (InvocationTargetException | IllegalAccessException ex) {
      throw new RuntimeException("Unable to split MotionEvent.", ex);
    }
  }

  private void removePointersFromTouchTargets(int pointerIdBits) {
    TouchTarget predecessor = null;
    TouchTarget target = firstHandlingTouchTarget;
    while (target != null) {
      TouchTarget next = target.next;
      if ((target.pointerIdBits & pointerIdBits) != 0) {
        target.pointerIdBits &= ~pointerIdBits;
        if (target.pointerIdBits == 0) {
          if (predecessor == null) {
            firstHandlingTouchTarget = next;
          } else {
            predecessor.next = next;
          }
          target = next;
          continue;
        }
      }
      predecessor = target;
      target = next;
    }
  }

  @Nullable
  private TouchTarget getTouchTargetForNode(Node node) {
    for (TouchTarget target = firstHandlingTouchTarget; target != null; target = target.next) {
      if (target.node == node) {
        return target;
      }
    }
    return null;
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Nullable
  private Node dispatchTouchEvent(
      MotionEvent motionEvent,
      HitTestResult hitTestResult,
      Node node,
      int desiredPointerIdBits,
      boolean bubble) {
    // Calculate the number of pointers to deliver.
    int eventPointerIdBits = getPointerIdBits(motionEvent);
    int finalPointerIdBits = eventPointerIdBits & desiredPointerIdBits;

    // If for some reason we ended up in an inconsistent state where it looks like we
    // might produce a motion event with no pointers in it, then drop the event.
    if (finalPointerIdBits == 0) {
      return null;
    }

    // Split the motion event if necessary based on the pointer Ids included in the event
    // compared to the pointer Ids that the node is handling.
    MotionEvent finalEvent = motionEvent;
    boolean needsRecycle = false;
    if (finalPointerIdBits != eventPointerIdBits) {
      finalEvent = splitMotionEvent(motionEvent, finalPointerIdBits);
      needsRecycle = true;
    }

    // Bubble the event up the hierarchy until a node handles the event, or the root is reached.
    Node resultNode = node;
    while (resultNode != null) {
      if (resultNode.dispatchTouchEvent(hitTestResult, finalEvent)) {
        break;
      } else {
        if (bubble) {
          resultNode = resultNode.getParent();
        } else {
          resultNode = null;
        }
      }
    }

    if (resultNode == null) {
      tryDispatchToSceneTouchListener(hitTestResult, finalEvent);
    }

    if (needsRecycle) {
      finalEvent.recycle();
    }

    return resultNode;
  }

  private int getPointerIdBits(MotionEvent motionEvent) {
    int idBits = 0;
    int pointerCount = motionEvent.getPointerCount();
    for (int i = 0; i < pointerCount; i++) {
      idBits |= 1 << motionEvent.getPointerId(i);
    }
    return idBits;
  }

  private TouchTarget addTouchTarget(Node node, int pointerIdBits) {
    final TouchTarget target = new TouchTarget();
    target.node = node;
    target.pointerIdBits = pointerIdBits;
    target.next = firstHandlingTouchTarget;
    firstHandlingTouchTarget = target;
    return target;
  }

  private void clearTouchTargets() {
    handlingTouchListener = null;
    firstHandlingTouchTarget = null;
  }
}
