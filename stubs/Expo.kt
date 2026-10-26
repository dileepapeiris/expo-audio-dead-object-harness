@file:Suppress("unused", "UNUSED_PARAMETER")
package expo.modules.kotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class JsLogger { fun warn(message: String, cause: Throwable? = null) { println("[jsLogger.warn] $message") } }

open class AppContext {
  // Unconfined so queued emits run right away in the harness.
  val mainQueue: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
  val jsLogger: JsLogger? = JsLogger()
}
