package fi.simoroikonen.lunchvote.components.occasion.domain

import com.google.protobuf.empty.Empty
import fi.simoroikonen.lunchvote.{ EntityId, JWT_ORGANIZATION_CLAIM_NAME, JWT_USERNAME_CLAIM_NAME }
import fi.simoroikonen.lunchvote.components.occasion.api
import fi.simoroikonen.lunchvote.components.occasion.domain.{ OccasionPublished, OccasionState, VotePlacedForOccasion }
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

import java.time.{ DateTimeException, Instant }

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Occasion(context: EventSourcedEntityContext) extends AbstractOccasion {
  override def emptyState: OccasionState = OccasionState.defaultInstance

  private def username: String =
    commandContext().metadata.get(s"_kalix-jwt-claim-$JWT_USERNAME_CLAIM_NAME").getOrElse("UNDEFINED")

  private def organization: String =
    commandContext().metadata.get(s"_kalix-jwt-claim-$JWT_ORGANIZATION_CLAIM_NAME").getOrElse("UNDEFINED")

  private def isValidDateTimeString(dateTimeString: String): Boolean =
    try {
      Instant.parse(dateTimeString)
      true
    } catch {
      case _: DateTimeException => false
    }

  // VALIDATION

  private def onError(currentState: OccasionState, cmd: api.PublishOccasionCommand): Option[String] =
    Option
      .when(currentState.id.length > 0)("Occasion already introduced")
      .orElse(Option.when(!isValidDateTimeString(cmd.startDatetimeIso8610Utc))(
        "startDatetimeIso8610Utc mut be in ISO 8601 UTC format e.g. '2023-01-01T13:00:00Z'"))

  private def onError(currentState: OccasionState, cmd: api.VotePlaceForOccasionCommand): Option[String] =
    Option
      .when(currentState.id.length == 0)("Occasion not existing")
      .orElse(
        Option.when(currentState.votes.find(_.voterUsername.equals(username)).nonEmpty)("Voter has already voted"))

  // COMMAND HANDLERS

  override def publish(
      currentState: OccasionState,
      cmd: api.PublishOccasionCommand): EventSourcedEntity.Effect[EntityId] =
    onError(currentState, cmd) match {
      case Some(error) => effects.error(error)
      case None =>
        effects
          .emitEvent(
            OccasionPublished(
              id = commandContext().entityId,
              organization = organization,
              areaId = cmd.areaId,
              startDatetimeIso8610Utc = cmd.startDatetimeIso8610Utc,
              publisherUsername = username))
          .thenReply(_ => EntityId(context.entityId))
    }

  override def votePlace(
      currentState: OccasionState,
      cmd: api.VotePlaceForOccasionCommand): EventSourcedEntity.Effect[Empty] =
    onError(currentState, cmd) match {
      case Some(error) => effects.error(error)
      case None =>
        effects
          .emitEvent(
            VotePlacedForOccasion(occasionId = cmd.occasionId, placeId = cmd.placeId, voterUsername = username))
          .thenReply(_ => Empty.defaultInstance)
    }

  // EVENT HANDLERS

  override def occasionPublished(currentState: OccasionState, event: OccasionPublished): OccasionState =
    currentState.copy(
      id = event.id,
      organization = event.organization,
      areaId = event.areaId,
      startDatetimeIso8610Utc = event.startDatetimeIso8610Utc,
      publisherUsername = event.publisherUsername)

  override def votePlacedForOccasion(currentState: OccasionState, event: VotePlacedForOccasion): OccasionState =
    currentState.copy(votes = currentState.votes :+ Vote(placeId = event.placeId, voterUsername = event.voterUsername))

}
