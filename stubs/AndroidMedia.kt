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

