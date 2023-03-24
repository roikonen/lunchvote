package fi.simoroikonen.lunchvote.components.area.view

import fi.simoroikonen.lunchvote.components.area.domain.{
  AreaIntroduced,
  AreaState,
  Place,
  PlaceArchived,
  PlaceIntroduced
}
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class AreasByIdView(context: ViewContext) extends AbstractAreasByIdView {

  override def emptyState: AreaState = AreaState.defaultInstance

  override def processAreaIntroduced(
      currentState: AreaState,
      event: AreaIntroduced
  ): UpdateEffect[AreaState] =
    effects.updateState(
      currentState.copy(organization = event.organization, id = event.id)
    )

  override def processPlaceIntroduced(
      currentState: AreaState,
      event: PlaceIntroduced
  ): UpdateEffect[AreaState] =
    effects.updateState(
      currentState.copy(places = currentState.places :+ Place(event.id))
    )

  override def processPlaceArchived(
      currentState: AreaState,
      event: PlaceArchived
  ): UpdateEffect[AreaState] =
    effects.updateState(
      currentState.copy(places = currentState.places.filterNot(_.id.equals(event.id)))
    )
}
