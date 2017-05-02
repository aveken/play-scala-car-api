package v1.caradvert

import javax.inject.Inject

import play.api.http.HttpVerbs
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * A wrapped request for  CarAdvert resources.
  *
  * This is commonly used to hold request-specific information like
  * security credentials, and useful shortcut methods.
  */
class CarAdvertRequest[A](request: Request[A], val messages: Messages)
    extends WrappedRequest(request)

/**
  * The default action for the  CarAdvert resource.
  *
  * This is the place to put logging, metrics, to augment
  * the request with contextual data, and manipulate the
  * result.
  */
class CarAdvertAction @Inject()(messagesApi: MessagesApi)(
    implicit ec: ExecutionContext)
    extends ActionBuilder[CarAdvertRequest]
    with HttpVerbs {

  type  CarAdvertRequestBlock[A] = CarAdvertRequest[A] => Future[Result]

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  override def invokeBlock[A](request: Request[A],
                              block:  CarAdvertRequestBlock[A]): Future[Result] = {
    if (logger.isTraceEnabled()) {
      logger.trace(s"invokeBlock: request = $request")
    }

    val messages = messagesApi.preferred(request)
    val future = block(new CarAdvertRequest(request, messages))

    future.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case other =>
          result
      }
    }
  }
}
