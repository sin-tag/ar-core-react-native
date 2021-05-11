// create by hoangtuyensk@gmail.com - github: sig-tag
#import "React/RCTViewManager.h"

@interface RCT_EXTERN_MODULE(ArCoreReactNativeViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(color, NSString)
//RCT_EXTERN_METHOD(testAction:(nonnull NSNumber *)node parameterName:(NSString *)nameObject)
RCT_EXTERN_METHOD(CMD_RUN_SET_OBJECT:(nonnull NSNumber *)node parameterName:(NSString *)nameObject parameterID:(NSString *)idProduct)
RCT_EXTERN_METHOD(CMD_RUN_DELETE_OBJECT:(nonnull NSNumber *)node)
RCT_EXTERN_METHOD(CMD_RUN_SET_DUPLICATE:(nonnull NSNumber *)node)
RCT_EXTERN_METHOD(CMD_RUN_GET_ID:(nonnull NSNumber *)node)
RCT_EXTERN_METHOD(CMD_RUN_KILL_PROCESS:(nonnull NSNumber *)node)
@end

