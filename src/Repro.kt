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

}
