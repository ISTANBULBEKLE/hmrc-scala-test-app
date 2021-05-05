package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture.failed
import akka.stream.ActorMaterializer
import models.DataModel
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.FakeRequest
import play.api.http.Status
import repositories.DataRepository

import scala.concurrent.{ExecutionContext, Future}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.commands.{LastError, WriteResult}
import reactivemongo.core.errors.GenericDriverException




class ApplicationControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar {
  val controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val mockDataRepository: DataRepository = mock[DataRepository]

  object TestApplicationController extends ApplicationController(
    controllerComponents,
    mockDataRepository,
    executionContext
  )


  implicit val system: ActorSystem = ActorSystem("Sys")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val dataModel: DataModel = DataModel(
    "abcd",
    "test name",
    "test description",
    100
  )

  "ApplicationController .index" should {

    "return OK" in{
      when(mockDataRepository.find(any())(any()))
          .thenReturn(Future(List(dataModel)))

      val result = TestApplicationController.index()(FakeRequest())
      status(result) shouldBe Status.OK
    }


    "the mongo data creation failed" should {

      val jsonBody: JsObject = Json.obj(
        "_id" -> "abcd",
        "name" -> "test name",
        "description" -> "test description",
        "numSales" -> 100
      )

      "return an error" in {

        when(mockDataRepository.find(any())(any()))
            .thenReturn(Future(failed(GenericDriverException("Error"))))

        val result = TestApplicationController.index()(FakeRequest())

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        await(bodyOf(result)) shouldBe Json.obj("message" -> "Error adding item to Mongo").toString()
      }
    }

  }

  "ApplicationController .create()" should {
    "ApplicationController .create" when {
      "the json body is valid" should {
        "return ???" in {
          val jsonBody: JsObject = Json.obj(
            "_id" -> "abcd",
            "name" -> "test name",
            "description" -> "test description",
            "numSales" -> 100
          )

          val writeResult: WriteResult = LastError(ok = true, None, None, None, 0, None, updatedExisting = false, None, None, wtimeout = false, None, None)

          when(mockDataRepository.create(any()))
            .thenReturn(Future(writeResult))

          val result = TestApplicationController.create()(FakeRequest().withBody(jsonBody))
          status(result) shouldBe Status.CREATED
        }
      }

      "the json body is not valid" should {
        "return BAD REQUEST" in {
          val jsonBody: JsObject = Json.obj(
            "_id" -> "abcd",
            "fail" -> "test name"
          )

          val writeResult: WriteResult = LastError(ok = true, None, None, None, 0, None, updatedExisting = false, None, None, wtimeout = false, None, None)
          val result = TestApplicationController.create()(FakeRequest().withBody(jsonBody))
          status(result) shouldBe Status.BAD_REQUEST
        }
      }
    }

    "the mongo data creation failed" should {

      val jsonBody: JsObject = Json.obj(
        "_id" -> "abcd",
        "name" -> "test name",
        "description" -> "test description",
        "numSales" -> 100
      )

      "return an error" in {
        when(mockDataRepository.create(any()))
            .thenReturn(Future.failed(GenericDriverException("Error")))

        val result = TestApplicationController.create()(FakeRequest().withBody(jsonBody))

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        await(bodyOf(result)) shouldBe Json.obj("message" -> "Error adding item to Mongo").toString()
      }
    }
  }

  "ApplicationController .read()" should {
    "return OK" in {
      when(mockDataRepository.read("_id":String))
          .thenReturn(Future(dataModel))

      val result = TestApplicationController.read("_id":String)(FakeRequest())
      status(result) shouldBe Status.OK
    }

    "the mongo data creation failed" should {

      val jsonBody: JsObject = Json.obj(
        "_id" -> "abcd",
        "name" -> "test name",
        "description" -> "test description",
        "numSales" -> 100
      )

      "return an error" in {

        when(mockDataRepository.read(any()))
            .thenReturn(Future(failed(GenericDriverException("Error"))))

        val result = TestApplicationController.read("_id":String)(FakeRequest())

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        await(bodyOf(result)) shouldBe Json.obj("message" -> "Error adding item to Mongo").toString()
      }
    }


  }

  "ApplicationController .update()" should {
        "the json body is valid" should {
          val jsonBody: JsObject = Json.obj(
            "_id" -> "abcd",
            "name" -> "test name",
            "description" -> "test description",
            "numSales" -> 100
          )

          "return <status code>" in {
            when(mockDataRepository.update(dataModel))
                .thenReturn(Future(dataModel))
            val result = TestApplicationController.update("_id":String)(FakeRequest().withBody(jsonBody))
            status(result) shouldBe Status.ACCEPTED
          }
          "return JSON body" in {
            val result = TestApplicationController.update("_id":String)(FakeRequest().withBody(jsonBody))
            await(jsonBodyOf(result)) shouldBe jsonBody
          }
        }

        "json body is not valid" should {
          val jsonBody: JsObject = Json.obj(
            "unexpected field" -> "foo"
          )
           "returned status code" in {
             val result = TestApplicationController.update("_id":String)(FakeRequest().withBody(jsonBody))
             status(result) shouldBe Status.BAD_REQUEST
          }

        }

    "the mongo data creation failed" should {

      val jsonBody: JsObject = Json.obj(
        "_id" -> "abcd",
        "name" -> "test name",
        "description" -> "test description",
        "numSales" -> 100
      )

      "return an error" in {
        when(mockDataRepository.update(any()))
            .thenReturn(Future.failed(GenericDriverException("Error")))

        val result = TestApplicationController.update("_id":String)(FakeRequest().withBody(jsonBody))

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        await(bodyOf(result)) shouldBe Json.obj("message" -> "Error adding item to Mongo").toString()
      }
    }

  }

  "ApplicationController .delete()" should {
    "return Accepted" in{
      val writeResult: WriteResult = LastError(ok = true, None, None, None, 0, None, updatedExisting = false, None, None, wtimeout = false, None, None)

      when(mockDataRepository.delete("_id":String))
          .thenReturn(Future(writeResult))

      val result = TestApplicationController.delete("_id":String)(FakeRequest())
      status(result) shouldBe Status.ACCEPTED
    }

    "the mongo data creation failed" should {

      val jsonBody: JsObject = Json.obj(
        "_id" -> "abcd",
        "name" -> "test name",
        "description" -> "test description",
        "numSales" -> 100
      )

      "return an error" in {

        when(mockDataRepository.delete("_id":String))
            .thenReturn(Future(failed(GenericDriverException("Error"))))

        val result = TestApplicationController.delete("_id":String)(FakeRequest())

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        await(bodyOf(result)) shouldBe Json.obj("message" -> "Error adding item to Mongo").toString()
      }
    }
  }


}
