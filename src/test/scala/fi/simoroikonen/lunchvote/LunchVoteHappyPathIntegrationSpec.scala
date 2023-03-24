package fi.simoroikonen.lunchvote

import fi.simoroikonen.lunchvote.components.occasion.api.{
  Occasion,
  Place,
  PublishOccasionCommand,
  VotePlaceForOccasionCommand
}
import fi.simoroikonen.lunchvote.controller.{
  AdminControllerActionClient,
  ByAreaRequest,
  IntroduceAreaCommand,
  IntroducePlaceCommand,
  IntroduceVoterCommand,
  OccasionsQuery,
  VoterControllerActionClient
}
import kalix.scalasdk.testkit.KalixTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{ Millis, Seconds, Span }
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LunchVoteHappyPathIntegrationSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  implicit private val patience: PatienceConfig =
    PatienceConfig(Span(5, Seconds), Span(500, Millis))

  private val testKit =
    KalixTestKit(Main.createKalix(), KalixTestKit.DefaultSettings.withAdvancedViews()).start()

  // Tokens created in https://jwt.io/
  // {
  //   "organization": "simo_org",
  //   "username": "admin"
  // }
  private val adminToken =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJvcmdhbml6YXRpb24iOiJzaW1vX29yZyIsInVzZXJuYW1lIjoiYWRtaW4ifQ.13DlUb6xNouozGrqA7PlOQW6jMI33XpTj0boyYuIknA"
  // {
  //   "organization": "simo_org",
  //   "username": "simo"
  // }
  private val simoToken =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJvcmdhbml6YXRpb24iOiJzaW1vX29yZyIsInVzZXJuYW1lIjoic2ltbyJ9.txIwdskDc_84AzE_xAHYFF5GXbnUmoOFP985BP3PKa4"
  // {
  //   "organization": "simo_org",
  //   "username": "seppo"
  // }
  private val seppoToken =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJvcmdhbml6YXRpb24iOiJzaW1vX29yZyIsInVzZXJuYW1lIjoic2VwcG8ifQ.g1wQX3xFBc5NrEtgItM8Ycl4awZr3qJ2NVEfqmRGoXs"

  val adminController = AdminControllerActionClient(testKit.grpcClientSettings)(testKit.system)
  val voterController = VoterControllerActionClient(testKit.grpcClientSettings)(testKit.system)

  "AreaService" must {

    "serve the happy path" in {

      // ADMIN: Create area and voters
      adminController
        .introduceArea()
        .addHeader("Authorization", s"Bearer $adminToken")
        .invoke(IntroduceAreaCommand("jyväskylä"))
        .futureValue
      adminController
        .introduceVoter()
        .addHeader("Authorization", s"Bearer $adminToken")
        .invoke(IntroduceVoterCommand(username = "simo", areaIds = Seq("jyväskylä")))
        .futureValue
      adminController
        .introduceVoter()
        .addHeader("Authorization", s"Bearer $adminToken")
        .invoke(IntroduceVoterCommand(username = "seppo", areaIds = Seq("jyväskylä")))
        .futureValue

      // SIMO: Create places (fitwok, basecamp)
      voterController
        .introducePlace()
        .addHeader("Authorization", s"Bearer $simoToken")
        .invoke(IntroducePlaceCommand(areaId = "jyväskylä", placeId = "fitwok"))
        .futureValue
      voterController
        .introducePlace()
        .addHeader("Authorization", s"Bearer $simoToken")
        .invoke(IntroducePlaceCommand(areaId = "jyväskylä", placeId = "basecamp"))
        .futureValue

      // SEPPO: Create places (shalimar, nom)
      voterController
        .introducePlace()
        .addHeader("Authorization", s"Bearer $seppoToken")
        .invoke(IntroducePlaceCommand(areaId = "jyväskylä", placeId = "shalimar"))
        .futureValue
      voterController
        .introducePlace()
        .addHeader("Authorization", s"Bearer $seppoToken")
        .invoke(IntroducePlaceCommand(areaId = "jyväskylä", placeId = "nom"))
        .futureValue

      // SIMO: Get publish occasion, get place IDs and vote
      val occasionId = voterController
        .publishOccasion()
        .addHeader("Authorization", s"Bearer $simoToken")
        .invoke(PublishOccasionCommand(areaId = "jyväskylä", startDatetimeIso8610Utc = "2023-01-01T13:00:00Z"))
        .futureValue
        .id
      eventually {
        val placeIds = voterController
          .getPlaceIds()
          .addHeader("Authorization", s"Bearer $simoToken")
          .invoke(ByAreaRequest("jyväskylä"))
          .futureValue
          .placeIds
        placeIds should contain("shalimar")
        placeIds
      }
      voterController
        .votePlaceForOccasion()
        .addHeader("Authorization", s"Bearer $simoToken")
        .invoke(VotePlaceForOccasionCommand(occasionId = occasionId, placeId = "shalimar"))

      // SEPPO: Get occasions and vote
      val occasionIdFromView = eventually {
        val occasions = voterController
          .getOccasions()
          .addHeader("Authorization", s"Bearer $seppoToken")
          .invoke(
            OccasionsQuery(
              startAfterIso8610Utc = "2023-01-01T12:00:00Z",
              startBeforeIso8610Utc = "2023-01-01T14:00:00Z"
            )
          )
          .futureValue
          .occasions
        occasions.map(_.id) shouldEqual Seq(occasionId)
        occasions
      }.map(_.id).head
      voterController
        .votePlaceForOccasion()
        .addHeader("Authorization", s"Bearer $seppoToken")
        .invoke(VotePlaceForOccasionCommand(occasionId = occasionIdFromView, placeId = "shalimar"))

      // SIMO: See the voting results
      val occasion = eventually {
        val occasions = voterController
          .getOccasions()
          .addHeader("Authorization", s"Bearer $simoToken")
          .invoke(
            OccasionsQuery(
              startAfterIso8610Utc = "2023-01-01T12:00:00Z",
              startBeforeIso8610Utc = "2023-01-01T14:00:00Z"
            )
          )
          .futureValue
          .occasions
        occasions.map(_.votedPlaces.headOption.map(_.votes)) shouldEqual Seq(Option(2))
        occasions.head
      }
      occasion.copy(id = "") shouldEqual Occasion(
        areaId = "jyväskylä",
        startDatetimeIso8610Utc = "2023-01-01T13:00:00Z",
        publisherUsername = "simo",
        votedPlaces = Seq(Place(placeId = "shalimar", votes = 2))
      )

    }

  }

  override def afterAll(): Unit = {
    testKit.stop()
    super.afterAll()
  }
}
