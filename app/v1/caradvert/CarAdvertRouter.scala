package v1.caradvert

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes and URLs to the  CarAdvertResource controller.
  */
class CarAdvertRouter @Inject()(controller: CarAdvertController) extends SimpleRouter {
  val prefix = "/v1/caradverts"

  def link(id: String): String = {
    import com.netaporter.uri.dsl._
    val url = prefix / id
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.process

    case GET(p"/$id") =>
      controller.show(id)
  }

}
