package fi.simoroikonen.lunchvote.controller

import com.google.protobuf.empty.Empty
import com.google.protobuf.timestamp.Timestamp
import fi.simoroikonen.lunchvote.components.area.view.ById
import fi.simoroikonen.lunchvote.{ EntityId, JWT_ORGANIZATION_CLAIM_NAME, JWT_USERNAME_CLAIM_NAME }
import fi.simoroikonen.lunchvote.components.{ area, occasion, voter }
import fi.simoroikonen.lunchvote.components.voter.domain.VoterState
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext
import org.slf4j.{ Logger, LoggerFactory }

import java.time.{ DateTimeException, Instant }
import scala.concurrent.Future

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class VoterControllerActionImpl(creationContext: ActionCreationContext) extends AbstractVoterControllerAction {

  private val log: Logger =
    LoggerFactory.getLogger(classOf[VoterControllerActionImpl])

  private def voterState(
      organization: String,
      voterUsername: String
  ): Future[Option[VoterState]] =
    components.voterByUsernameView
      .getVoters(voter.view.ByUsernameRequest(organization, voterUsername))
      .execute()
      .map(_.voters.headOption)

  private def isAuthorized: Future[Boolean] = {
    val isAuthorizedF = isValidUsername
    isAuthorizedF.map { isAuthorized =>
      if (!isAuthorized)
        log.warn(
          s"User '${usernameOption.getOrElse("UNDEFINED")}' in organization '${organizationOption
            .getOrElse("UNDEFINED")}' is not authorized to act as a voter"
        )
    }
    isAuthorizedF
  }

  private def isValidUsername: Future[Boolean] =
    organizationAndUsernameOption
      .map(r => voterState(r._1, r._2).map(_.nonEmpty))
      .getOrElse(Future.successful(false))

  private def usernameOption: Option[String] =
    actionContext.metadata.get(s"_kalix-jwt-claim-${JWT_USERNAME_CLAIM_NAME}")

  private def organizationOption: Option[String] =
    actionContext.metadata.get(
      s"_kalix-jwt-claim-${JWT_ORGANIZATION_CLAIM_NAME}"
    )

  private def organizationAndUsernameOption: Option[(String, String)] =
    organizationOption match {
      case Some(organization) =>
        usernameOption match {
          case Some(username) => Option((organization, username))
          case None           => None
        }
      case None => None
    }

  private def isValidDateTimeString(dateTimeString: String): Boolean =
    try {
      Instant.parse(dateTimeString)
      true
    } catch {
      case _: DateTimeException => false
    }

  private def areaExists(
      organization: String,
      areaId: String
  ): Future[Boolean] =
    components.areaActionsImpl
      .areaExists(area.action.AreaExistsRequest(organization, areaId))
      .execute()
      .map(_.response)

  private def placeExistsInArea(
      organization: String,
      areaId: String,
      placeId: String
  ): Future[Boolean] =
    components.areaActionsImpl
      .placeExists(
        area.action.PlaceExistsRequest(organization, areaId, placeId)
      )
      .execute()
      .map(_.response)

  private def occasionByIdResponse(
      organization: String,
      occasionId: String
  ): Future[occasion.view.OccasionsByIdResponse] =
    components.occasionsByIdView
      .getOccasions(occasion.view.ById(organization, occasionId))
      .execute()

  private def voterAreaIds(
      organization: String,
      voterUsername: String
  ): Future[Seq[String]] =
    voterState(organization, voterUsername).map(
      _.map(_.areaIds).getOrElse(Seq.empty)
    )

  private def voterBelongsToArea(
      organization: String,
      voterUsername: String,
      areaId: String
  ): Future[Boolean] =
    voterState(organization, voterUsername).map(
      _.map(_.areaIds.contains(areaId)).getOrElse(false)
    )

  // VALIDATION

  private def onError(cmd: IntroducePlaceCommand): Future[Option[String]] = {
    def process(
        isAuthorized: Boolean,
        voterBelongsToArea: Boolean
    ): Option[String] =
      Option
        .when(!isAuthorized)("Unauthorized")
        .orElse(
          Option.when(!voterBelongsToArea)(
            "Voter does not belong to the given area"
          )
        )

    val isAuthorizedF = isAuthorized
    val voterBelongsToAreaF = organizationAndUsernameOption
      .map(r => voterBelongsToArea(r._1, r._2, cmd.areaId))
      .getOrElse(Future.successful(false))
    for {
      isAuthorized       <- isAuthorizedF
      voterBelongsToArea <- voterBelongsToAreaF
    } yield process(isAuthorized, voterBelongsToArea)
  }

  private def onError(cmd: ArchivePlaceCommand): Future[Option[String]] = {
    def process(
        isAuthorized: Boolean,
        voterBelongsToArea: Boolean
    ): Option[String] =
      Option
        .when(!isAuthorized)("Unauthorized")
        .orElse(
          Option.when(!voterBelongsToArea)(
            "Voter does not belong to the given area"
          )
        )

    val isAuthorizedF = isAuthorized
    val voterBelongsToAreaF = organizationAndUsernameOption
      .map(r => voterBelongsToArea(r._1, r._2, cmd.areaId))
      .getOrElse(Future.successful(false))
    for {
      isAuthorized       <- isAuthorizedF
      voterBelongsToArea <- voterBelongsToAreaF
    } yield process(isAuthorized, voterBelongsToArea)
  }

  private def onError(query: ByAreaRequest): Future[Option[String]] = {
    def process(
        isAuthorized: Boolean,
        voterBelongsToArea: Boolean
    ): Option[String] =
      Option
        .when(!isAuthorized)("Unauthorized")
        .orElse(
          Option.when(!voterBelongsToArea)(
            "Voter does not belong to the given area"
          )
        )

    val isAuthorizedF = isAuthorized
    val voterBelongsToAreaF = organizationAndUsernameOption
      .map(r => voterBelongsToArea(r._1, r._2, query.areaId))
      .getOrElse(Future.successful(false))
    for {
      isAuthorized       <- isAuthorizedF
      voterBelongsToArea <- voterBelongsToAreaF
    } yield process(isAuthorized, voterBelongsToArea)
  }

  private def onError(
      cmd: occasion.api.PublishOccasionCommand
  ): Future[Option[String]] = {

    def process(
        isAuthorized: Boolean,
        areaExists: Boolean,
        voterBelongsToArea: Boolean
    ): Option[String] =
      Option
        .when(!isAuthorized)("Unauthorized")
        .orElse(
          Option.when(cmd.areaId.length == 0)(
            "Area ID must be at least one character long"
          )
        )
        .orElse(Option.when(!areaExists)("Area does not exist"))
        .orElse(
          Option.when(!voterBelongsToArea)(
            "Voter does not belong to the given area"
          )
        )

    val isAuthorizedF = isAuthorized
    val areaExistsF = organizationOption
      .map(areaExists(_, cmd.areaId))
      .getOrElse(Future.successful(false))
    val voterBelongsToAreaF = organizationAndUsernameOption
      .map(r => voterBelongsToArea(r._1, r._2, cmd.areaId))
      .getOrElse(Future.successful(false))
    for {
      isAuthorized       <- isAuthorizedF
      areaExists         <- areaExistsF
      voterBelongsToArea <- voterBelongsToAreaF
    } yield process(isAuthorized, areaExists, voterBelongsToArea)
  }

  private def onError(
      cmd: occasion.api.VotePlaceForOccasionCommand
  ): Future[Option[String]] = {
    def process(
        isAuthorized: Boolean,
        placeExistsInArea: Boolean,
        voterBelongsToArea: Boolean
    ): Option[String] =
      Option
        .when(!isAuthorized)("Unauthorized")
        .orElse(
          Option.when(!placeExistsInArea)(
            "Place does not exist in the area of the occasion"
          )
        )
        .orElse(
          Option.when(!voterBelongsToArea)(
            "Voter does not belong to the area of the occasion"
          )
        )

    val isAuthorizedF = isAuthorized
    val occasionAreaIdF = organizationOption
      .map(
        occasionByIdResponse(_, cmd.occasionId)
          .map(_.occasions.headOption.map(_.areaId).getOrElse(""))
      )
      .getOrElse(Future.successful(""))
    val occasionPlaceId = cmd.placeId
    for {
      isAuthorized   <- isAuthorizedF
      occasionAreaId <- occasionAreaIdF
      placeExistsInArea <- organizationOption
        .map(placeExistsInArea(_, occasionAreaId, occasionPlaceId))
        .getOrElse(Future.successful(false))
      voterBelongsToArea <- organizationAndUsernameOption
        .map(r => voterBelongsToArea(r._1, r._2, occasionAreaId))
        .getOrElse(Future.successful(false))
    } yield process(isAuthorized, placeExistsInArea, voterBelongsToArea)
  }

  private def onError(queryEnvelope: OccasionsQuery): Future[Option[String]] = {

    def process(isAuthorized: Boolean): Option[String] =
      Option
        .when(!isAuthorized)("Unauthorized")
        .orElse(
          Option.when(
            !isValidDateTimeString(queryEnvelope.startAfterIso8610Utc)
          )(
            "startAfterIso8610Utc mut be in ISO 8601 UTC format e.g. '2023-01-01T13:00:00Z'"
          )
        )
        .orElse(
          Option.when(
            !isValidDateTimeString(queryEnvelope.startBeforeIso8610Utc)
          )(
            "startBeforeIso8610Utc mut be in ISO 8601 UTC format e.g. '2023-01-01T13:00:00Z'"
          )
        )

    for {
      isAuthorized <- isAuthorized
    } yield process(isAuthorized)
  }

  // REQUEST HANDLER

  override def introducePlace(
      cmd: IntroducePlaceCommand
  ): Action.Effect[Empty] =
    effects.asyncEffect(onError(cmd).map {
      case Some(error) => effects.error(error)
      case None =>
        effects.forward(
          components.area
            .introducePlace(
              area.api
                .IntroducePlaceCommand(
                  organization = organizationOption.get,
                  areaId = cmd.areaId,
                  placeId = cmd.placeId
                )
            )
            .withMetadata(actionContext.metadata)
        )
    })

  override def archivePlace(cmd: ArchivePlaceCommand): Action.Effect[Empty] =
    effects.asyncEffect(onError(cmd).map {
      case Some(error) => effects.error(error)
      case None =>
        effects.forward(
          components.area
            .archivePlace(
              area.api
                .ArchivePlaceCommand(
                  organization = organizationOption.get,
                  areaId = cmd.areaId,
                  placeId = cmd.placeId
                )
            )
            .withMetadata(actionContext.metadata)
        )
    })

  override def getPlaceIds(
      query: ByAreaRequest
  ): Action.Effect[GetPlaceIdsResponse] =
    effects.asyncEffect(onError(query).map {
      case Some(error) => effects.error(error)
      case None =>
        effects.asyncReply(
          organizationOption
            .map(organization =>
              components.areasByIdView
                .getAreas(ById(organization = organization, id = query.areaId))
                .execute()
                .map(
                  _.areas.headOption
                    .map(_.places.map(_.id))
                    .getOrElse(Seq.empty)
                )
            )
            .getOrElse(Future.successful(Seq.empty))
            .map(GetPlaceIdsResponse(_))
        )
    })

  override def publishOccasion(
      cmd: occasion.api.PublishOccasionCommand
  ): Action.Effect[EntityId] =
    effects.asyncEffect(onError(cmd).map {
      case Some(error) => effects.error(error)
      case None =>
        effects.forward(
          components.occasion.publish(cmd).withMetadata(actionContext.metadata)
        )
    })

  override def votePlaceForOccasion(
      cmd: occasion.api.VotePlaceForOccasionCommand
  ): Action.Effect[Empty] =
    effects.asyncEffect(onError(cmd).map {
      case Some(error) => effects.error(error)
      case None =>
        effects.forward(
          components.occasion
            .votePlace(cmd)
            .withMetadata(actionContext.metadata)
        )
    })

  override def getOccasions(
      queryEnvelope: OccasionsQuery
  ): Action.Effect[occasion.view.OccasionsByAreasResponse] = {
    val errorOptionF = onError(queryEnvelope)
    val areaIdsF =
      organizationAndUsernameOption
        .map(r => voterAreaIds(r._1, r._2))
        .getOrElse(Future.successful(Seq.empty))

    effects.asyncEffect(for {
      errorOption <- errorOptionF
      areaIds     <- areaIdsF
    } yield {
      lazy val query: occasion.view.ByAreaIds = occasion.view.ByAreaIds(
        organization = organizationOption.get,
        areaIds = areaIds,
        startAfter = Option(Timestamp(Instant.parse(queryEnvelope.startAfterIso8610Utc))),
        startBefore = Option(Timestamp(Instant.parse(queryEnvelope.startBeforeIso8610Utc)))
      )
      errorOption
        .map(effects.error(_))
        .getOrElse(
          effects.forward(
            components.recentOccasionsByAreaView.getOccasionsByAreas(query)
          )
        )
    })
  }

}
