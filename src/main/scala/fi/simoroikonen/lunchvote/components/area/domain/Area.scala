package fi.simoroikonen.lunchvote.components.area.domain

import com.google.protobuf.empty.Empty
import fi.simoroikonen.lunchvote.JWT_USERNAME_CLAIM_NAME
import fi.simoroikonen.lunchvote.components.area.api
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class Area(context: EventSourcedEntityContext) extends AbstractArea {
  override def emptyState: AreaState = AreaState.defaultInstance

  private def username: String =
    commandContext().metadata
      .get(s"_kalix-jwt-claim-$JWT_USERNAME_CLAIM_NAME")
      .getOrElse("UNDEFINED")

  // VALIDATION

  private def onError(
      currentState: AreaState,
      cmd: api.IntroduceAreaCommand
  ): Option[String] =
    Option
      .when(currentState.id.length > 0)("Area already introduced")
      .orElse(
        Option.when(cmd.id.length < 0)(
          "Area ID must be at least one character long"
        )
      )

  private def onError(
      currentState: AreaState,
      cmd: api.IntroducePlaceCommand
  ): Option[String] =
    Option
      .when(currentState.id.length == 0)("Area not existing")
      .orElse(
        Option.when(cmd.placeId.length < 0)(
          "Place ID must be at least one character long"
        )
      )
      .orElse(
        Option.when(currentState.places.find(_.id == cmd.placeId).nonEmpty)(
          "Place already introduced"
        )
      )

  private def onError(
      currentState: AreaState,
      cmd: api.ArchivePlaceCommand
  ): Option[String] =
    Option
      .when(currentState.id.length == 0)("Area not existing")
      .orElse(
        Option.when(currentState.places.find(_.id == cmd.placeId).isEmpty)(
          "Place not existing"
        )
      )

  // COMMAND HANDLERS

  override def introduceArea(
      currentState: AreaState,
      cmd: api.IntroduceAreaCommand
  ): EventSourcedEntity.Effect[Empty] =
    onError(currentState, cmd) match {
      case Some(error) => effects.error(error)
      case None =>
        effects
          .emitEvent(
            AreaIntroduced(organization = cmd.organization, id = cmd.id)
          )
          .thenReply(_ => Empty.defaultInstance)
    }

  override def introducePlace(
      currentState: AreaState,
      cmd: api.IntroducePlaceCommand
  ): EventSourcedEntity.Effect[Empty] =
    onError(currentState, cmd) match {
      case Some(error) => effects.error(error)
      case None =>
        effects
          .emitEvent(PlaceIntroduced(cmd.placeId, username))
          .thenReply(_ => Empty.defaultInstance)
    }

  override def archivePlace(
      currentState: AreaState,
      cmd: api.ArchivePlaceCommand
  ): EventSourcedEntity.Effect[Empty] =
    onError(currentState, cmd) match {
      case Some(error) => effects.error(error)
      case None =>
        effects
          .emitEvent(PlaceArchived(cmd.placeId))
          .thenReply(_ => Empty.defaultInstance)
    }

  // EVENT HANDLERS

  override def areaIntroduced(
      currentState: AreaState,
      event: AreaIntroduced
  ): AreaState =
    currentState.copy(organization = event.organization, id = event.id)

  override def placeIntroduced(
      currentState: AreaState,
      event: PlaceIntroduced
  ): AreaState =
    currentState.copy(places =
      currentState.places :+ Place(
        id = event.id,
        introducerUsername = event.introducerUsername
      )
    )

  override def placeArchived(
      currentState: AreaState,
      event: PlaceArchived
  ): AreaState =
    currentState.copy(places =
      currentState.places.filterNot(_.id.equals(event.id))
    )

}
