LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := libssl
LOCAL_SRC_FILES := ../armeabi/libssl.a

include $(PREBUILT_STATIC_LIBRARY)

