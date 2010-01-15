/*
Copyright 2007-2009 WebDriver committers
Copyright 2007-2009 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

#ifndef interactions_h
#define interactions_h

#ifdef _MSC_VER
#include "stdafx.h"
#include "interaction_utils.h"
#endif

#include <wchar.h>

#ifdef _MSC_VER
#define EXPORT __declspec(dllexport)
#define WD_RESULT LRESULT
#define BOOL_TYPE boolean
#else
#define EXPORT
#define WD_RESULT int
#define BOOL_TYPE bool
#endif

#define WINDOW_HANDLE void*

#ifdef __cplusplus
extern "C" {
#endif

// Keyboard interactions
EXPORT void sendKeys(WINDOW_HANDLE windowHandle, const wchar_t* value, int timePerKey);
EXPORT BOOL_TYPE pending_keyboard_events();

// Mouse interactions
EXPORT WD_RESULT clickAt(WINDOW_HANDLE directInputTo, long x, long y);
EXPORT WD_RESULT mouseDownAt(WINDOW_HANDLE directInputTo, long x, long y);
EXPORT WD_RESULT mouseUpAt(WINDOW_HANDLE directInputTo, long x, long y);
EXPORT WD_RESULT mouseMoveTo(WINDOW_HANDLE directInputTo, long duration, long fromX, long fromY, long toX, long toY);

#ifdef __cplusplus
}
#endif
#endif
