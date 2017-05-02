package v1.caradvert

import java.util.Date
import javax.inject.Inject

import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class CarAdvertFormInput(title: String, fuel: String, price: Int, new_ : Boolean, mileage: Option[Int], firstRegistration: Option[Date])

/**
  * Takes HTTP requests and produces JSON.
  */
class CarAdvertController @Inject()(
                                     action: CarAdvertAction,
                                     handler: CarAdvertResourceHandler)(implicit ec: ExecutionContext)
  extends Controller {

  private val form: Form[CarAdvertFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "fuel" -> nonEmptyText,
        "price" -> number,
        "new" -> boolean,
        "mileage" -> optional(number),
        "first_registration" -> optional(date)
      )(CarAdvertFormInput.apply)(CarAdvertFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = {
    action.async { implicit request =>
      handler.find.map {  carAdverts =>
        Ok(Json.toJson( carAdverts))
      }
    }
  }

  def process: Action[AnyContent] = {
    action.async { implicit request =>
      processJsonCarAdvert()
    }
  }

  def show(id: String): Action[AnyContent] = {
    action.async { implicit request =>
      handler.lookup(id).map { carAdvert =>
        Ok(Json.toJson(carAdvert))
      }
    }
  }

  private def processJsonCarAdvert[A]()(
    implicit request: CarAdvertRequest[A]): Future[Result] = {
    def failure(badForm: Form[CarAdvertFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: CarAdvertFormInput) = {
      handler.create(input).map { carAdvert =>
        Created(Json.toJson(carAdvert)).withHeaders(LOCATION -> carAdvert.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
