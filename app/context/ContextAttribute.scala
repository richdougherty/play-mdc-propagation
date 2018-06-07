package context

import java.util.{Map => JMap}
import play.api.libs.typedmap.TypedKey
import play.api.mvc._

/** Helper for attaching a context to a request */
object RequestContext {

  val Attr = TypedKey[ContextRef]("ContextRef")

  /** Read the context from the request and attach into the current thread */
  def withRequestContext[A](rh: RequestHeader)(f: => A): A = {
    val ref = rh.attrs(Attr)
    Context.withRef(ref) { f }
  }

}