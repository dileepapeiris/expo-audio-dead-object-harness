@file:Suppress("unused", "UNUSED_PARAMETER")
package expo.modules.kotlin.sharedobjects
import expo.modules.kotlin.AppContext

open class SharedObject(val appContext: AppContext? = null) {
  val events = mutableListOf<Pair<String, Any?>>()
  fun emit(event: String, payload: Any?) { synchronized(events) { events += event to payload } }
  open fun sharedObjectDidRelease() {}
}
