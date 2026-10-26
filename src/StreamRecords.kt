package expo.modules.audio

import expo.modules.kotlin.exception.CodedException
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import expo.modules.kotlin.types.Enumerable
import expo.modules.kotlin.types.OptimizedRecord
import java.net.URL

enum class AudioStreamEncoding(val value: String) : Enumerable { FLOAT32("float32"), INT16("int16") }

@OptimizedRecord
class AudioStreamOptions : Record {
  @Field var sampleRate: Int = 48000
  @Field var channels: Int = 1
  @Field var encoding: AudioStreamEncoding = AudioStreamEncoding.FLOAT32
}

enum class AudioStreamFileFormat(val value: String) : Enumerable {
  WAV("wav"), PCM("pcm");
  val fileExtension: String get() = value
}

