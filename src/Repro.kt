package harness

import android.media.AudioRecord
import expo.modules.audio.AudioStream
import expo.modules.audio.AudioStreamOptions
import expo.modules.kotlin.AppContext

fun main() {
  val stream = AudioStream(AppContext(), AudioStreamOptions())
  stream.start()
  val recorder = requireNotNull(AudioRecord.lastInstance) { "no AudioRecord was created" }
  recorder.deadAfterReads = 3
  Thread.sleep(200)

  // By now read() has returned ERROR_DEAD_OBJECT. Watch what the capture loop does next.
  val callsAtDeath = recorder.readCalls
  Thread.sleep(300)
  val extraReads = recorder.readCalls - callsAtDeath
  val statuses = stream.events.filter { it.first == "audioStreamStatus" }.map { (it.second as Map<*, *>)["isStreaming"] }
  val buffers = stream.events.count { it.first == "audioStreamBuffer" }

  println("read() calls in the 300 ms after ERROR_DEAD_OBJECT: $extraReads")
  println("isStreaming after the dead object: ${stream.isStreaming}")
  println("recorder released: ${recorder.released}")
  println("status events: $statuses, buffer events: $buffers")
  val stopped = extraReads == 0 && !stream.isStreaming && statuses.lastOrNull() == false && recorder.released
  println(if (stopped) "PASS: the stream stopped after ERROR_DEAD_OBJECT" else "FAIL: the stream keeps running after ERROR_DEAD_OBJECT")
  stream.stop()
  kotlin.system.exitProcess(if (stopped) 0 else 1)
}
