#!/bin/zsh
# Usage: ./run.sh <path to AudioStream.kt>
# Compiles expo-audio's Android AudioStream.kt against JVM stubs and checks what the capture loop
# does after AudioRecord.read() returns ERROR_DEAD_OBJECT (expo/expo#52261). Needs kotlinc and JDK 17.
set -e
cd "$(dirname "$0")"
export JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
CO=$(find ~/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm -name "kotlinx-coroutines-core-jvm-1.8.0.jar" | head -1)
