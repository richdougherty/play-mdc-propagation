package context

import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import java.util.concurrent.atomic.AtomicReference
import java.util.{Map => JMap}
import play.api.mvc._
import scala.concurrent.Future

@Singleton
class ContextFilter @Inject() (implicit override val mat: Materializer) extends Filter {

  override def apply(next: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    // Create an empty context
    val ref: ContextRef = new AtomicReference[ContextMap]()

    // Attach the context to the current request
    val newRequestHeader = rh.withAttrs(rh.attrs.updated(RequestContext.Attr, ref))

    // Also attach the context to the current thread
    RequestContext.withRequestContext(newRequestHeader) {

      // Call the next stage with the header containing the context
      next(newRequestHeader)

    }
  }

}