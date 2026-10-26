package expo.modules.audio

import expo.modules.kotlin.exception.CodedException
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import expo.modules.kotlin.types.Enumerable
import expo.modules.kotlin.types.OptimizedRecord
import java.net.URL

enum class AudioStreamEncoding(val value: String) : Enumerable { FLOAT32("float32"), INT16("int16") }

