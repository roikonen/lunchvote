package fi.simoroikonen.lunchvote.controller

import com.google.protobuf.empty.Empty
import fi.simoroikonen.lunchvote.components.area.view.{ GetAreaIdsRequest, GetAreaIdsResponse }
import fi.simoroikonen.lunchvote.{ JWT_ORGANIZATION_CLAIM_NAME, JWT_USERNAME_CLAIM_NAME }
import fi.simoroikonen.lunchvote.components.{ area, voter }
import fi.simoroikonen.lunchvote.view.{ AreasWithVotersRequest, AreasWithVotersResults }
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.Future

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class AdminControllerActionImpl(creationContext: ActionCreationContext) extends AbstractAdminControllerAction {

  private val log: Logger = LoggerFactory.getLogger(classOf[AdminControllerActionImpl])

  private def isAuthorized: Boolean = {
    val isAuthorized = isValidUsername
    if (!isAuthorized)
      log.warn(s"User '${usernameOption.getOrElse("UNDEFINED")}' is not authorized to act as an administrator")
    isAuthorized
  }

  private def isValidUsername: Boolean = usernameOption.filter(_.equals("admin")).map(_ => true).getOrElse(false)

  private def areaExists(organization: String, areaId: String): Future[Boolean] =
    components.areaActionsImpl.areaExists(area.action.AreaExistsRequest(organization, areaId)).execute().map(_.response)

  private def usernameOption: Option[String] =
    actionContext.metadata.get(s"_kalix-jwt-claim-${JWT_USERNAME_CLAIM_NAME}")

  private def organizationOption: Option[String] =
    actionContext.metadata.get(s"_kalix-jwt-claim-${JWT_ORGANIZATION_CLAIM_NAME}")

  // VALIDATION

  private def onAuthError(): Future[Option[String]] =
    Future.successful(
      Option
        .when(!isAuthorized)("Unauthorized")
        .orElse(Option.when(organizationOption.isEmpty)("User's organization is not defined")))

  private def onError(cmd: IntroduceVoterCommand): Future[Option[String]] = {

    def process(areasExists: Boolean): Option[String] = {
      Option
        .when(!isAuthorized)("Unauthorized")
        .orElse(Option.when(organizationOption.isEmpty)("User's organization is not defined"))
        .orElse(Option.when(!areasExists)("All given areas don't exist"))
    }

    for {
      areasExists <- Future
        .sequence(
          cmd.areaIds.map(areaId => organizationOption.map(areaExists(_, areaId)).getOrElse(Future.successful(false))))
        .map(!_.contains(false))
    } yield process(areasExists)
  }

  // REQUEST HANDLER

  override def introduceArea(cmd: IntroduceAreaCommand): Action.Effect[Empty] =
    effects.asyncEffect(onAuthError().map {
      case Some(error) => effects.error(error)
      case None =>
        effects.forward(
          components.area
            .introduceArea(area.api.IntroduceAreaCommand(organization = organizationOption.get, id = cmd.id))
            .withMetadata(actionContext.metadata))
    })

  override def getAreaIds(empty: Empty): Action.Effect[GetAreaIdsResponse] =
    effects.asyncEffect(onAuthError().map {
      case Some(error) => effects.error(error)
      case None =>
        effects.forward(components.areaIdsView.getAreaIds(GetAreaIdsRequest(organization = organizationOption.get)))
    })

  override def getAreasWithPlacesAndVoters(empty: Empty): Action.Effect[AreasWithVotersResults] =
    effects.asyncEffect(onAuthError().map {
      case Some(error) => effects.error(error)
      case None =>
        effects.forward(
          components.areasWithVotersJoinViewImpl.getAreas(
            AreasWithVotersRequest(organization = organizationOption.get)))
    })

  override def introduceVoter(cmd: IntroduceVoterCommand): Action.Effect[Empty] =
    effects.asyncEffect(onError(cmd).map {
      case Some(error) => effects.error(error)
      case None =>
        effects.forward(
          components.voter
            .introduceVoter(
              voter.api.IntroduceVoterCommand(
                organization = organizationOption.get,
                username = cmd.username,
                areaIds = cmd.areaIds))
            .withMetadata(actionContext.metadata))
    })

  override def archiveVoter(cmd: ArchiveVoterCommand): Action.Effect[Empty] =
    effects.asyncEffect(onAuthError().map {
      case Some(error) => effects.error(error)
      case None =>
        effects.forward(
          components.voter
            .archiveVoter(voter.api.ArchiveVoterCommand(organization = organizationOption.get, username = cmd.username))
            .withMetadata(actionContext.metadata))
    })

}
