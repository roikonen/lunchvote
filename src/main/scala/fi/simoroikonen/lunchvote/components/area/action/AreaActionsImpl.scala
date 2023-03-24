package fi.simoroikonen.lunchvote.components.area.action

import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext
import fi.simoroikonen.lunchvote.components.area

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class AreaActionsImpl(creationContext: ActionCreationContext) extends AbstractAreaActionsAction {

  override def areaExists(
      request: AreaExistsRequest
  ): Action.Effect[BoolResponse] =
    effects.asyncReply(
      components.areasByIdView
        .getAreas(area.view.ById(request.organization, request.areaId))
        .execute()
        .map(_.areas.nonEmpty)
        .map(BoolResponse(_))
    )

  override def placeExists(
      request: PlaceExistsRequest
  ): Action.Effect[BoolResponse] =
    effects.asyncReply(
      components.areasByIdView
        .getAreas(area.view.ById(request.organization, request.areaId))
        .execute()
        .map(
          _.areas.headOption
            .map(_.places.find(_.id.equals(request.placeId)).nonEmpty)
            .getOrElse(false)
        )
        .map(BoolResponse(_))
    )
}
