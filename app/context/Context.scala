package context

import java.util.concurrent.atomic.AtomicReference
import java.util.Map
import org.slf4j.MDC

object Context {

  // Store the ref in the local thread so we can propagate it automatically
  private val threadLocal = new ThreadLocal[ContextRef]

  /** Sets the reference and the MDC in the thread then calls the given code. */
  def withRef[A](ref: ContextRef)(f: => A): A = {
    val oldRef = getFromThread
    setInThread(ref)
    try f finally setInThread(oldRef)
  }

  /** Sets both the reference and MDC in the thread. */
  def setInThread(ref: ContextRef): Unit = {
    threadLocal.set(ref)
    setContextMapInThread(ref.get)
  }

  /** Gets the reference from the thread, updating it with the current MDC. */
  def getFromThread: ContextRef = {
    val ref = threadLocal.get()
    val mdc = getContextMapFromThread
    if (ref == null) {
      // To keep our logic elsewhere simple create a ref here even if it probably won't be used
      new ContextRef(mdc)
    } else {
      updateRef(ref, mdc)
      ref
    }
  }

  /** Update a reference with the given MDC value. */
  private def updateRef(ref: ContextRef, newMap: ContextMap): Unit = {
    ref.set(newMap) // TODO: Consider merging map entries?
  }

  private def setContextMapInThread(contextMap: ContextMap): Unit = {
    if (contextMap == null || contextMap.isEmpty) {
      MDC.clear()
    } else {
      MDC.setContextMap(contextMap)
    }
  }

  private def getContextMapFromThread: ContextMap = {
    val contextMap = MDC.getCopyOfContextMap()
    if (contextMap == null || contextMap.isEmpty) null else contextMap
  }

}