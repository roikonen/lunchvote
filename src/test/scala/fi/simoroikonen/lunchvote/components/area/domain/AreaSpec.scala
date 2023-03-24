package fi.simoroikonen.lunchvote.components.area.domain

import com.google.protobuf.empty.Empty
import fi.simoroikonen.lunchvote.JWT_USERNAME_CLAIM_NAME
import fi.simoroikonen.lunchvote.components.area.api
import io.grpc.Status
import kalix.scalasdk.Metadata
import kalix.scalasdk.testkit.EventSourcedResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class AreaSpec extends AnyWordSpec with Matchers {

  "The Area" should {

    val testUsername         = "testUsername"
    val jwtUsernameClaimName = s"_kalix-jwt-claim-$JWT_USERNAME_CLAIM_NAME"

    val organization = "organization"
    val areaId       = "areaId"
    val placeId      = "placeId"

    "correctly process commands of type IntroduceArea" in {
      val testKit = AreaTestKit(new Area(_))
      val result: EventSourcedResult[Empty] = testKit.introduceArea(
        api.IntroduceAreaCommand(organization = organization, id = areaId)
      )
      result.didEmitEvents shouldEqual true
      result.events shouldEqual List(
        AreaIntroduced(
          organization = organization,
          id = areaId
        )
      )
    }

    "reject IntroduceArea command if command fields are lacking info" in {
      val testKit = AreaTestKit(new Area(_))

      def verify(result: EventSourcedResult[Empty]) = {
        result.isError shouldEqual true
        result.didEmitEvents shouldEqual false
      }

      verify(
        testKit.introduceArea(
          api.IntroduceAreaCommand(id = areaId)
        )
      )
      verify(
        testKit.introduceArea(
          api.IntroduceAreaCommand(organization = organization)
        )
      )

    }

    "reject IntroduceArea if area has already been introduced" in {
      val testKit = AreaTestKit(new Area(_))

      // Prerequisite
      testKit.introduceArea(api.IntroduceAreaCommand(organization = organization, id = areaId))

      val result: EventSourcedResult[Empty] =
        testKit.introduceArea(api.IntroduceAreaCommand(organization = organization, id = areaId))
      result.isError shouldEqual true
      result.errorStatusCode shouldEqual Status.Code.ALREADY_EXISTS
    }

    "correctly process commands of type IntroducePlace" in {
      val testKit = AreaTestKit(new Area(_))

      val cmdMetadata = Metadata.empty.add(jwtUsernameClaimName, testUsername)

      // Prerequisite
      testKit.introduceArea(api.IntroduceAreaCommand(organization = organization, id = areaId))

      val result: EventSourcedResult[Empty] = testKit.introducePlace(
        api.IntroducePlaceCommand(organization = organization, areaId = areaId, placeId = placeId),
        cmdMetadata
      )
      result.didEmitEvents shouldEqual true
      result.events shouldEqual List(
        PlaceIntroduced(id = placeId, introducerUsername = testUsername)
      )
    }

    "reject IntroducePlace if the area of the place does not exist" in {
      val testKit = AreaTestKit(new Area(_))

      val cmdMetadata = Metadata.empty.add(jwtUsernameClaimName, testUsername)

      val result: EventSourcedResult[Empty] = testKit.introducePlace(
        api.IntroducePlaceCommand(organization = organization, areaId = areaId, placeId = placeId),
        cmdMetadata
      )
      result.isError shouldEqual true
      result.errorStatusCode shouldEqual Status.Code.FAILED_PRECONDITION
    }

    "reject IntroducePlace if the place already exist" in {
      val testKit = AreaTestKit(new Area(_))

      val cmdMetadata = Metadata.empty.add(jwtUsernameClaimName, testUsername)

      // Prerequisite
      testKit.introduceArea(api.IntroduceAreaCommand(organization = organization, id = areaId))
      testKit.introducePlace(
        api.IntroducePlaceCommand(organization = organization, areaId = areaId, placeId = placeId),
        cmdMetadata
      )

      val result: EventSourcedResult[Empty] = testKit.introducePlace(
        api.IntroducePlaceCommand(organization = organization, areaId = areaId, placeId = placeId),
        cmdMetadata
      )
      result.isError shouldEqual true
      result.errorStatusCode shouldEqual Status.Code.ALREADY_EXISTS
    }

    "reject IntroducePlace command if command fields are lacking info" in {
      val testKit = AreaTestKit(new Area(_))

      // Prerequisite
      testKit.introduceArea(api.IntroduceAreaCommand(organization = organization, id = areaId))

      def verify(result: EventSourcedResult[Empty]) = {
        result.isError shouldEqual true
        result.didEmitEvents shouldEqual false
      }

      val cmdMetadata = Metadata.empty.add(jwtUsernameClaimName, testUsername)

      verify(
        testKit.introducePlace(
          api.IntroducePlaceCommand(areaId = areaId, placeId = placeId),
          cmdMetadata
        )
      )
      verify(
        testKit.introducePlace(
          api.IntroducePlaceCommand(organization = organization, placeId = placeId),
          cmdMetadata
        )
      )
      verify(
        testKit.introducePlace(
          api.IntroducePlaceCommand(organization = organization, areaId = areaId),
          cmdMetadata
        )
      )
    }

    "correctly process commands of type ArchivePlace" in {
      val testKit = AreaTestKit(new Area(_))

      val cmdMetadata = Metadata.empty.add(jwtUsernameClaimName, testUsername)

      // Prerequisite
      testKit.introduceArea(api.IntroduceAreaCommand(organization = organization, id = areaId))
      testKit.introducePlace(
        api.IntroducePlaceCommand(organization = organization, areaId = areaId, placeId = placeId),
        cmdMetadata
      )

      val result: EventSourcedResult[Empty] =
        testKit.archivePlace(api.ArchivePlaceCommand(organization = organization, areaId = areaId, placeId = placeId))
      result.didEmitEvents shouldEqual true
      result.events shouldEqual List(PlaceArchived(id = placeId))
    }

    "reject ArchivePlace if the place does not exist" in {
      val testKit = AreaTestKit(new Area(_))

      val cmdMetadata = Metadata.empty.add(jwtUsernameClaimName, testUsername)

      // Prerequisite
      testKit.introduceArea(api.IntroduceAreaCommand(organization = organization, id = areaId))

      val result: EventSourcedResult[Empty] = testKit.archivePlace(
        api.ArchivePlaceCommand(organization = organization, areaId = areaId, placeId = placeId)
      )
      result.isError shouldEqual true
      result.errorStatusCode shouldEqual Status.Code.NOT_FOUND
    }

    "reject ArchivePlace if the place has already been archived" in {
      val testKit = AreaTestKit(new Area(_))

      val cmdMetadata = Metadata.empty.add(jwtUsernameClaimName, testUsername)

      // Prerequisite
      testKit.introduceArea(api.IntroduceAreaCommand(organization = organization, id = areaId))
      testKit.introducePlace(
        api.IntroducePlaceCommand(organization = organization, areaId = areaId, placeId = placeId),
        cmdMetadata
      )
      testKit.archivePlace(api.ArchivePlaceCommand(organization = organization, areaId = areaId, placeId = placeId))

      val result: EventSourcedResult[Empty] = testKit.archivePlace(
        api.ArchivePlaceCommand(organization = organization, areaId = areaId, placeId = placeId)
      )
      result.isError shouldEqual true
      result.errorStatusCode shouldEqual Status.Code.NOT_FOUND
    }

    "reject ArchivePlace command if command fields are lacking info" in {
      val testKit = AreaTestKit(new Area(_))

      val cmdMetadata = Metadata.empty.add(jwtUsernameClaimName, testUsername)

      // Prerequisite
      testKit.introduceArea(api.IntroduceAreaCommand(organization = organization, id = areaId))
      testKit.introducePlace(
        api.IntroducePlaceCommand(organization = organization, areaId = areaId, placeId = placeId),
        cmdMetadata
      )

      def verify(result: EventSourcedResult[Empty]) = {
        result.isError shouldEqual true
        result.didEmitEvents shouldEqual false
      }

      verify(testKit.archivePlace(api.ArchivePlaceCommand(areaId = areaId, placeId = placeId)))
      verify(testKit.archivePlace(api.ArchivePlaceCommand(organization = organization, placeId = placeId)))
      verify(testKit.archivePlace(api.ArchivePlaceCommand(organization = organization, areaId = areaId)))

    }
  }
}
