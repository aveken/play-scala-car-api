package v1.caradvert

import java.text.SimpleDateFormat
import java.util.{Date, UUID}
import javax.inject.{Inject, Provider}

import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying CarAdvert information.
  */
case class CarAdvertResource(id: String, link: String, title: String, fuel: String, price: Int, new_ : Boolean, mileage: Option[Int], firstRegistration: Option[Date])

object CarAdvertResource {

  implicit object dateFormat extends Format[Date] {
    val format = new SimpleDateFormat("yyyy-MM-dd")

    def reads(json: JsValue) = {
      val str = json.as[String]
      if (str != null)
        JsSuccess(format.parse(str))
      else
        JsSuccess(null)
    }

    def writes(date: Date) = if (date !=null ) JsString(format.format(date)) else JsNull
  }


  /**
    * Mapping to write a CarAdvertResource out as a JSON value.
    */
  implicit val implicitWrites = new Writes[CarAdvertResource] {
    def writes(carAdvert: CarAdvertResource): JsValue = {
      Json.obj(
        "id" -> carAdvert.id,
        "link" -> carAdvert.link,
        "title" -> carAdvert.title,
        "fuel" -> carAdvert.fuel,
        "price" -> carAdvert.price,
        "new" -> carAdvert.new_,
        "mileage" -> carAdvert.mileage,
        "first_registration" -> carAdvert.firstRegistration
      )
    }
  }
}

/**
  * Controls access to the backend data, returning [[CarAdvertResource]]
  */
class CarAdvertResourceHandler @Inject()(
                                          routerProvider: Provider[CarAdvertRouter],
                                          carAdvertRepository: CarAdvertRepository)(implicit ec: ExecutionContext) {

  def create(input: CarAdvertFormInput): Future[CarAdvertResource] = {
    val data = CarAdvertData(UUID.randomUUID().toString, input.title, input.fuel, input.price, input.new_, input.mileage, input.firstRegistration)
    // We don't actually create the carAdvert, so return what we have
    carAdvertRepository.create(data).map { id =>
      createCarAdvertResource(data)
    }
  }

  def lookup(id: String): Future[Option[CarAdvertResource]] = {
    val  carAdvertFuture = carAdvertRepository.get(id)
     carAdvertFuture.map { maybeCarAdvertData =>
      maybeCarAdvertData.map {  carAdvertData =>
        createCarAdvertResource( carAdvertData)
      }
    }
  }

  def find: Future[Iterable[CarAdvertResource]] = {
    carAdvertRepository.list().map {  carAdvertDataList =>
       carAdvertDataList.map( carAdvertData => createCarAdvertResource( carAdvertData))
    }
  }

  private def createCarAdvertResource(carAdvert: CarAdvertData): CarAdvertResource = {
    CarAdvertResource(carAdvert.id, routerProvider.get.link(carAdvert.id), carAdvert.title, carAdvert.fuel, carAdvert.price, carAdvert.new_, carAdvert.mileage, carAdvert.firstRegistration)
  }

}
