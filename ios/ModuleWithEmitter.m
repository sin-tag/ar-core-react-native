//
//  ModuleWithEmitter.m
//
//
//  // create by hoangtuyensk@gmail.com - github: sig-tag
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "ModuleWithEmitter.h"

@implementation ModuleWithEmitter
RCT_EXPORT_MODULE();
+ (BOOL)requiresMainQueueSetup {
  return YES;
}

+ (id)allocWithZone:(NSZone *)zone {
  static ModuleWithEmitter *sharedInstance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    sharedInstance = [super allocWithZone:zone];
  });
  return sharedInstance;
}

+ (void)sendEventWithName:(NSString *)name body:(NSDictionary*)body {
  [[ModuleWithEmitter allocWithZone:nil] sendEventWithName:name body:body];
}

- (NSArray<NSString *> *)supportedEvents {
  return @[EMIT_GET_PATH_FILE_SCREEN_SHORT, EMIT_GET_NAME, EMIT_GET_TEST];
}

@end
