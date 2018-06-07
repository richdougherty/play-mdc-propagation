// root package

import context.ContextFilter
import javax.inject.{Inject, Singleton}
import play.api.http.{DefaultHttpFilters, HttpFilters}
import play.api.mvc._

@Singleton
class Filters @Inject() (contextFilter: ContextFilter) extends DefaultHttpFilters(contextFilter)
