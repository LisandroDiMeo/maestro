#!/bin/bash

adb shell uiautomator dump && adb pull /sdcard/window_dump.xml $1
