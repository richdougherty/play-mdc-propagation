import java.util.concurrent.atomic.AtomicReference
import java.util.{HashMap => JHashMap, Map => JMap}

package object context {
  type ContextMap = JMap[String, String]
  type ContextRef = AtomicReference[ContextMap]
}