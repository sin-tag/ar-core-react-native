// create by hoangtuyensk@gmail.com - github: sig-tag
#import "React/RCTViewManager.h"

@interface RCT_EXTERN_MODULE(ArCoreReactNativeViewManager, RCTViewManager)

// Declarative props
RCT_EXPORT_VIEW_PROPERTY(planeDetection, NSString)
RCT_EXPORT_VIEW_PROPERTY(lightEstimation, BOOL)
RCT_EXPORT_VIEW_PROPERTY(showPlanes, BOOL)
RCT_EXPORT_VIEW_PROPERTY(showFeaturePoints, BOOL)

// Event callbacks
RCT_EXPORT_VIEW_PROPERTY(onObjectSelected, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onScreenshot, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onPlaneDetected, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onTrackingStateChange, RCTDirectEventBlock)

// Imperative commands
RCT_EXTERN_METHOD(addObject:(nonnull NSNumber *)node modelPath:(NSString *)modelPath objectId:(NSString *)objectId)
RCT_EXTERN_METHOD(removeSelectedObject:(nonnull NSNumber *)node)
RCT_EXTERN_METHOD(setDuplicate:(nonnull NSNumber *)node)
RCT_EXTERN_METHOD(takeScreenshot:(nonnull NSNumber *)node)
RCT_EXTERN_METHOD(reset:(nonnull NSNumber *)node)

@end
