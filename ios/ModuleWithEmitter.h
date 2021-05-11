//
//  ModuleWithEmitter.h
//  
//
//  // create by hoangtuyensk@gmail.com - github: sig-tag
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#define EMIT_GET_PATH_FILE_SCREEN_SHORT @"EMIT_GET_PATH_FILE_SCREEN_SHORT"
#define EMIT_GET_TEST @"EMIT_GET_TEST"
#define EMIT_GET_NAME @"EMIT_GET_NAME"

@interface ModuleWithEmitter : RCTEventEmitter <RCTBridgeModule>

+ (id)allocWithZone:(NSZone *)zone;
+ (void)sendEventWithName:(NSString *)name body:(NSDictionary*)body;


@end
