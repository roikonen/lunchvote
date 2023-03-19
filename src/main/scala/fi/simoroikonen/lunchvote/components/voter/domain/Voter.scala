package fi.simoroikonen.lunchvote.components.voter.domain

import com.google.protobuf.empty.Empty
import fi.simoroikonen.lunchvote.JWT_ORGANIZATION_CLAIM_NAME
import fi.simoroikonen.lunchvote.components.voter.api
import fi.simoroikonen.lunchvote.components.voter.domain.{ VoterArchived, VoterIntroduced, VoterState }
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Voter(context: EventSourcedEntityContext) extends AbstractVoter {
  override def emptyState: VoterState = VoterState.defaultInstance

  private def organization: String =
    commandContext().metadata.get(s"_kalix-jwt-claim-$JWT_ORGANIZATION_CLAIM_NAME").getOrElse("UNDEFINED")

  // VALIDATION

  private def onError(currentState: VoterState, cmd: api.IntroduceVoterCommand): Option[String] =
    Option
      .when(currentState.username.length > 0)("Voter already introduced")
      .orElse(Option.when(cmd.username.length < 0)("Voter's username must be at least one character long"))
      .orElse(Option.when(cmd.areaIds.size == 0)("Voter needs to belong at least to one area"))

  private def onError(currentState: VoterState, cmd: api.ArchiveVoterCommand): Option[String] =
    Option.when(currentState.username.length == 0)("Voter not existing")

  // COMMAND HANDLERS

  override def introduceVoter(
      currentState: VoterState,
      cmd: api.IntroduceVoterCommand): EventSourcedEntity.Effect[Empty] =
    onError(currentState, cmd) match {
      case Some(error) => effects.error(error)
      case None =>
        effects
          .emitEvent(VoterIntroduced(organization = organization, username = cmd.username, areaIds = cmd.areaIds))
          .thenReply(_ => Empty.defaultInstance)
    }

  override def archiveVoter(currentState: VoterState, cmd: api.ArchiveVoterCommand): EventSourcedEntity.Effect[Empty] =
    onError(currentState, cmd) match {
      case Some(error) => effects.error(error)
      case None =>
        effects
          .emitEvent(
            VoterArchived(organization = organization, username = cmd.username, areaIds = currentState.areaIds))
          .thenReply(_ => Empty.defaultInstance)
    }

  // EVENT HANDLERS

  override def voterIntroduced(currentState: VoterState, voterIntroduced: VoterIntroduced): VoterState =
    currentState.copy(username = voterIntroduced.username, areaIds = voterIntroduced.areaIds, archived = false)

  override def voterArchived(currentState: VoterState, voterArchived: VoterArchived): VoterState =
    currentState.copy(archived = true)

}
