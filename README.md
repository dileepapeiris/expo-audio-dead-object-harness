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

Results on 10/26/26 with the expo-audio 58.0.10 sources:

| What | `main` | fix (expo/expo PR) |
| --- | --- | --- |
| `read()` calls in the 300 ms after the dead object | 13 | 0 |
| `isStreaming` | still `true` | `false` |
| recorder released | no | yes |
| status events | `[true]`, no `false` | `[true, false]` |
| verdict | FAIL | PASS |

On `main` the loop keeps polling the invalid recorder and nothing tells the app that capture
stopped. With the fix the loop stops on the first negative read result, releases the recorder and
emits the final status.

## Review round 2 on 10/28/26

The review of expo/expo#52300 asked for three changes: name the error code in the warning, let a
capture loop that no longer owns the recorder leave quietly, and guard `start()`/`stop()` with
a lock. `src/Repro.kt` now runs three scenarios:

1. **ERROR_DEAD_OBJECT from read()**: the original check, plus the warning must name the code.
2. **stop() + start() while the old loop is inside read()**: the old loop's read returns
   `ERROR_INVALID_OPERATION` on the released recorder. It must not warn and must not stop the
   new stream.
3. **Concurrent stop()**: 200 rounds of `stop()` from a second thread while the loop stops
   itself after a dead read. Each recorder must be stopped and released once, with one
   `isStreaming: false`.

| Scenario | PR before the review | PR after the review |
| --- | --- | --- |
| 1 | stops, but the warning says `failed with -6` | PASS, `failed with error code: -6 (ERROR_DEAD_OBJECT)` |
| 2 | FAIL: the old loop stops the new stream, status events `[true, false, true, false]`, new recorder released | PASS: no warning, status events `[true, false, true]`, new stream keeps reading |
| 3 | PASS | PASS |

Scenario 3 also passes before the lock: the window between the `isStreaming` check and the
assignment is too small to hit on the JVM, so it is a regression guard, not proof of the race.
