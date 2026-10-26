@file:Suppress("unused", "UNUSED_PARAMETER")
package android.media

import java.nio.ByteBuffer

object AudioFormat {
  const val CHANNEL_IN_MONO = 16
  const val CHANNEL_IN_STEREO = 12
  const val ENCODING_PCM_16BIT = 2
  const val ENCODING_PCM_FLOAT = 4
}

object MediaRecorder { object AudioSource { const val MIC = 1 } }

/** Stand-in for android.media.AudioRecord. The harness controls stop() and observes release(). */
open class AudioRecord(audioSource: Int, sampleRate: Int, channelConfig: Int, audioFormat: Int, bufferSize: Int) {
  companion object {
    const val STATE_INITIALIZED = 1
    const val READ_BLOCKING = 0
    const val ERROR = -1
    const val ERROR_BAD_VALUE = -2
    const val ERROR_DEAD_OBJECT = -6
    @JvmStatic fun getMinBufferSize(sampleRate: Int, channelConfig: Int, audioFormat: Int): Int = 4096
    @Volatile var lastInstance: AudioRecord? = null
  }
  init { lastInstance = this }
  var throwOnStop = false
  @Volatile var released = false
  var stopCalls = 0
  /** After this many successful reads, read() returns ERROR_DEAD_OBJECT (-6) forever. */
  @Volatile var deadAfterReads = Int.MAX_VALUE
  @Volatile var readCalls = 0
  val state: Int get() = STATE_INITIALIZED
  open fun startRecording() {}
  open fun stop() {
    stopCalls += 1
    if (throwOnStop) throw IllegalStateException("stop() called on an uninitialized AudioRecord.")
  }
  open fun release() { released = true }
  open fun read(buffer: ByteBuffer, sizeInBytes: Int, readMode: Int): Int {
    Thread.sleep(20)
    readCalls += 1
    if (readCalls > deadAfterReads) return ERROR_DEAD_OBJECT
    val n = minOf(64, sizeInBytes)
    for (i in 0 until n) buffer.put(i, 0)
    return n
  }
}
