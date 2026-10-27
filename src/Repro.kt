package harness

import android.media.AudioRecord
import expo.modules.audio.AudioStream
import expo.modules.audio.AudioStreamOptions
import expo.modules.kotlin.AppContext
import kotlin.concurrent.thread

private fun statuses(stream: AudioStream) =
  stream.events.filter { it.first == "audioStreamStatus" }.map { (it.second as Map<*, *>)["isStreaming"] }

/** read() returns ERROR_DEAD_OBJECT: the loop must stop the stream, release the recorder and tell JS. */
fun deadObject(): Boolean {
  println("== 1. ERROR_DEAD_OBJECT from read()")
  val stream = AudioStream(AppContext(), AudioStreamOptions())
  stream.start()
  val recorder = requireNotNull(AudioRecord.lastInstance) { "no AudioRecord was created" }
  recorder.deadAfterReads = 3
  Thread.sleep(200)

  // By now read() has returned ERROR_DEAD_OBJECT. Watch what the capture loop does next.
  val callsAtDeath = recorder.readCalls
  Thread.sleep(300)
  val extraReads = recorder.readCalls - callsAtDeath
  val warnings = stream.appContext?.jsLogger?.warnings.orEmpty()

  println("read() calls in the 300 ms after ERROR_DEAD_OBJECT: $extraReads")
  println("isStreaming after the dead object: ${stream.isStreaming}")
  println("recorder released: ${recorder.released}")
  println("status events: ${statuses(stream)}, buffer events: ${stream.events.count { it.first == "audioStreamBuffer" }}")
  println("warnings: $warnings")
  val stopped = extraReads == 0 && !stream.isStreaming && statuses(stream).lastOrNull() == false && recorder.released
  val named = warnings.size == 1 && warnings[0].contains("-6 (ERROR_DEAD_OBJECT)")
  println(if (stopped && named) "PASS: the stream stopped after ERROR_DEAD_OBJECT and the warning names it" else "FAIL: stopped=$stopped namedWarning=$named")
  stream.stop()
  return stopped && named
}

/**
 * JS calls stop() and start() while the old loop is inside read(). The old loop's read then fails
 * on the released recorder (ERROR_INVALID_OPERATION). It must leave quietly: no warning, and the
 * new stream keeps running.
 */
fun staleLoopAfterRestart(): Boolean {
  println("== 2. stop() + start() while the old loop is inside read()")
  val stream = AudioStream(AppContext(), AudioStreamOptions())
  stream.start()
  val first = requireNotNull(AudioRecord.lastInstance)
  first.readMillis = 150
  Thread.sleep(60) // the loop is now blocked inside read()
  stream.stop()
  stream.start()
  val second = requireNotNull(AudioRecord.lastInstance)
  check(second !== first) { "start() did not create a new recorder" }
  Thread.sleep(400) // the old read returns ERROR_INVALID_OPERATION during this window

  val warnings = stream.appContext?.jsLogger?.warnings.orEmpty()
  println("isStreaming after the restart: ${stream.isStreaming}")
  println("new recorder released: ${second.released}, reads on the new recorder: ${second.readCalls}")
  println("status events: ${statuses(stream)}")
  println("warnings: $warnings")
  val ok = stream.isStreaming && !second.released && second.readCalls > 0 &&
    statuses(stream) == listOf(true, false, true) && warnings.isEmpty()
  println(if (ok) "PASS: the old loop left without touching the new stream" else "FAIL: the old loop stopped or warned about the new stream")
  stream.stop()
  return ok
}

/**
 * stop() from the JS thread and stop() from the capture loop at the same time, 200 rounds. With
 * the state lock each recorder is stopped once, released once, and no call throws.
 */
fun concurrentStops(): Boolean {
  println("== 3. concurrent stop() from two threads, 200 rounds")
  var thrown = 0
  var doubleStops = 0
  var doubleStatus = 0
  repeat(200) {
    val stream = AudioStream(AppContext(), AudioStreamOptions())
    stream.start()
    val recorder = requireNotNull(AudioRecord.lastInstance)
    recorder.readMillis = 1
    recorder.deadAfterReads = 2 // the loop will call stop() on its own
    val js = thread {
      Thread.sleep(2)
      try { stream.stop() } catch (e: Exception) { thrown += 1 }
    }
    js.join()
    Thread.sleep(30)
    if (recorder.stopCalls != 1) doubleStops += 1
    if (statuses(stream) != listOf(true, false)) doubleStatus += 1
    if (!recorder.released || stream.isStreaming) thrown += 1000
  }
  println("rounds with an exception or a leaked recorder: $thrown, with stop() called twice on one recorder: $doubleStops, with a repeated status event: $doubleStatus")
  val ok = thrown == 0 && doubleStops == 0 && doubleStatus == 0
  println(if (ok) "PASS: every round stopped the recorder exactly once" else "FAIL")
  return ok
}

fun main() {
  val results = listOf(deadObject(), staleLoopAfterRestart(), concurrentStops())
  println()
  println("summary: " + results.mapIndexed { i, r -> "${i + 1}=${if (r) "PASS" else "FAIL"}" }.joinToString(" "))
  kotlin.system.exitProcess(if (results.all { it }) 0 else 1)
}
