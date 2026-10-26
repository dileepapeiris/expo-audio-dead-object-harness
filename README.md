# JVM harness for expo/expo#52261

Checks what expo-audio's Android `AudioStream` capture loop does after `AudioRecord.read()` returns
`ERROR_DEAD_OBJECT` (-6). Compiles the real `AudioStream.kt` and `AudioStreamFileWriter.kt` against
small stubs of the Android and Expo classes they use. The `AudioRecord` stub returns real data for
three reads and `ERROR_DEAD_OBJECT` from then on.

```sh
./run.sh ../expo/packages/expo-audio/android/src/main/java/expo/modules/audio/AudioStream.kt
```

Expected: the loop stops reading the dead recorder, `isStreaming` becomes `false`, a status event
with `isStreaming: false` is emitted, and the recorder is released.

Result on 10/26/26 with the expo-audio 58.0.10 sources from `main`:

| What | Observed |
| --- | --- |
| `read()` calls in the 300 ms after the dead object | 13 |
| `isStreaming` | still `true` |
| recorder released | no |
| status events | `[true]`, no `false` |

The loop keeps polling the invalid recorder and nothing tells the app that capture stopped.
