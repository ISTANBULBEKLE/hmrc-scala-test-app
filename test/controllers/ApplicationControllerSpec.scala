package controllers

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.FakeRequest
import play.api.http.Status


class ApplicationControllerSpec extends UnitSpec with GuiceOneAppPerSuite {
  val controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]

  object TestApplicationController extends ApplicationController(
    controllerComponents
  )


  "ApplicationController .index" should {

    val result = TestApplicationController.index()(FakeRequest())

    "return TODO" in {
      status(result) shouldBe Status.OK
    }
  }

  "ApplicationController .create()" should {

  }

  "ApplicationController .read()" should {

  }

  "ApplicationController .update()" should {

  }

  "ApplicationController .delete()" should {

  }


}
