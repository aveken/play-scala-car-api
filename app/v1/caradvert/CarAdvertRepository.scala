package v1.caradvert

import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.{Inject, Singleton}

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Item}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}

import scala.concurrent.Future

final case class CarAdvertData(id: String, title: String, fuel: String, price: Int, new_ : Boolean, mileage: Option[Int], firstRegistration: Option[Date])


/**
  * A pure non-blocking interface for the CarAdvertRepository.
  */
trait CarAdvertRepository {
  def create(data: CarAdvertData): Future[String]

  def list(): Future[Iterable[CarAdvertData]]

  def get(id: String): Future[Option[CarAdvertData]]
}

/**
  * A trivial implementation for the CarAdvert Repository.
  */
@Singleton
class CarAdvertRepositoryImpl @Inject() extends CarAdvertRepository {

  var tableName = "CarAdvert"
  val client: AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "eu-central-1")).build
  val dynamoDB = new DynamoDB(client)

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  override def list(): Future[Iterable[CarAdvertData]] = {
    Future.successful {
      logger.trace(s"list: ")
      List[CarAdvertData]()
    }
  }

  override def get(id: String): Future[Option[CarAdvertData]] = {
    Future.successful {
      logger.trace(s"get: id = $id")
      val table = dynamoDB.getTable(tableName)
      val item = table.getItem("Id", id)
      if (item != null) {
        val firstRegistration = if (!item.isPresent("FirstRegistration") || item.isNull("FirstRegistration")) None else Some(dateFormat.parse(item.getString("FirstRegistration")))
        val mileage = if (!item.isPresent("Mileage") || item.isNull("Mileage")) None else Some(item.getInt("Mileage"))
        Some(CarAdvertData(item.getString("Id"), item.getString("Title"), item.getString("Fuel"), item.getInt("Price"), item.getBoolean("New"), mileage, firstRegistration))
      } else
        None
    }
  }

  def create(data: CarAdvertData): Future[String] = Future.successful {
    logger.trace(s"create: data = $data")
    val table = dynamoDB.getTable(tableName)
    val item = new Item().withPrimaryKey("Id", data.id).withString("Title", data.title).withNumber("Price", data.price).withString("Fuel", data.fuel).withBoolean("New", data.new_)
    data.firstRegistration.map {
      firstRegistration => item.withString("FirstRegistration", dateFormat.format(firstRegistration))
    }
    data.mileage.map {
      mileage => item.withNumber("Mileage", mileage)
    }
    table.putItem(item)
    data.id
  }
}
